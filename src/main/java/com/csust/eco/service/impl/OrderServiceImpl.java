package com.csust.eco.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.csust.eco.common.BizException;
import com.csust.eco.dto.OrderCreateDTO;
import com.csust.eco.entity.Item;
import com.csust.eco.entity.Orders;
import com.csust.eco.mapper.ItemMapper;
import com.csust.eco.mapper.OrdersMapper;
import com.csust.eco.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrderService {

    private final ItemMapper itemMapper;
    private final RedissonClient redissonClient;

    @Override
    public Long createOrder(OrderCreateDTO dto, Long buyerId) {
        Long itemId = dto.getItemId();
        String lockKey = "eco:lock:item:" + itemId;
        RLock lock = redissonClient.getLock(lockKey);

        boolean isLocked = false;
        try {
            // 尝试获取锁, 0秒等待, 触发看门狗机制
            isLocked = lock.tryLock(0, -1, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new BizException("当前商品抢购人数过多, 请重试");
            }

            // 获取 AOP 代理对象, 保护底层事务
            OrderService proxy = (OrderService) AopContext.currentProxy();
            return proxy.createOrderInTx(itemId, buyerId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("系统繁忙, 下单失败");
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long createOrderInTx(Long itemId, Long buyerId) {
        Item item = itemMapper.selectById(itemId);

        if (item == null) {
            throw new BizException("商品不存在");
        }
        if (item.getStock() < 1 || item.getStatus() != 0) {
            throw new BizException("手慢一步, 商品已售出或下架");
        }
        if (item.getSellerId().equals(buyerId)) {
            throw new BizException("不能购买自己发布的商品");
        }

        // CAS 乐观锁扣减 (Compare And Swap)
        // 底层 SQL: UPDATE item SET stock=0, status=1 WHERE id=? AND stock=1 AND status=0
        LambdaUpdateWrapper<Item> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Item::getId, itemId)
                .eq(Item::getStock, 1)      // 比较: 只有库存在此刻仍为 1 时
                .eq(Item::getStatus, 0)     // 比较: 且状态仍为 0 (待售) 时
                .set(Item::getStock, 0)     // 交换: 扣减库存
                .set(Item::getStatus, 1);   // 交换: 锁定状态

        int updatedRows = itemMapper.update(null, updateWrapper);
        if (updatedRows == 0) {
            throw new BizException("手慢一步, 商品已被抢走");
        }

        Orders order = new Orders();
        order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        order.setBuyerId(buyerId);
        order.setItemId(itemId);
        order.setAmount(item.getPrice());
        order.setStatus((byte) 0);

        this.save(order);

        // 将订单抛入延迟队列
        RBlockingQueue<Long> blockingQueue = redissonClient.getBlockingQueue("eco:queue:order:cancel");
        RDelayedQueue<Long> delayedQueue = redissonClient.getDelayedQueue(blockingQueue);
        delayedQueue.offer(order.getId(), 15, TimeUnit.MINUTES);
        log.info("订单 {} 已加入延迟队列, 15分钟后若未支付将自动回滚", order.getId());

        return order.getId();
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancelOrder(Long orderId, Long buyerId) {
        // 1. 极致轻量的投影查询：只拿我们需要的回滚凭据 (itemId)，拒绝使用 SELECT *
        Orders order = this.getOne(new LambdaQueryWrapper<Orders>()
                .select(Orders::getItemId)
                .eq(Orders::getId, orderId));

        if (order == null) {
            throw new BizException("订单不存在");
        }

        // 2. 核心原子写入与鉴权：将身份验证与状态流转合并为一条物理 SQL
        // 对应底层: UPDATE orders SET status = 4 WHERE id = ? AND buyer_id = ? AND status = 0
        LambdaUpdateWrapper<Orders> orderUpdateWrapper = new LambdaUpdateWrapper<>();
        orderUpdateWrapper.eq(Orders::getId, orderId)
                .eq(Orders::getBuyerId, buyerId) // 物理级水平越权防御
                .eq(Orders::getStatus, 0)        // 物理级状态机防御 (只允许取消待支付)
                .set(Orders::getStatus, 4);

        int orderRows = this.baseMapper.update(null, orderUpdateWrapper);
        if (orderRows == 0) {
            // 如果影响行数为 0，意味着在极短的时间窗口内，状态被其他线程改了，或者当前用户根本不是买家
            throw new BizException("订单取消失败：权限不足或订单状态已发生变更");
        }

        // 3. 物理回滚商品库存 (订单取消成功后，强关联的库存必须回滚)
        rollbackItemStock(order.getItemId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancelOrderSystem(Long orderId) {
        // 1. 同样获取投影
        Orders order = this.getOne(new LambdaQueryWrapper<Orders>()
                .select(Orders::getItemId)
                .eq(Orders::getId, orderId));

        if (order == null) return;

        // 2. 系统级自动取消，无需校验 buyerId，仅严格校验状态机
        LambdaUpdateWrapper<Orders> orderUpdateWrapper = new LambdaUpdateWrapper<>();
        orderUpdateWrapper.eq(Orders::getId, orderId)
                .eq(Orders::getStatus, 0) // 确保只有超时未支付的才会被系统取消
                .set(Orders::getStatus, 4);

        int orderRows = this.baseMapper.update(null, orderUpdateWrapper);
        if (orderRows > 0) {
            // 只有当 UPDATE 真实发生了修改，才去执行回滚，防止并发环境下的重复回滚
            rollbackItemStock(order.getItemId());
            log.info("系统已成功执行超时订单自动回滚: {}", orderId);
        }
    }

    /**
     * 抽离公共的库存回滚原子操作
     */
    private void rollbackItemStock(Long itemId) {
        LambdaUpdateWrapper<Item> itemUpdateWrapper = new LambdaUpdateWrapper<>();
        itemUpdateWrapper.eq(Item::getId, itemId)
                .eq(Item::getStatus, 1)  // 核心修复：只有商品处于"1-交易中"时，才允许回滚！
                .set(Item::getStock, 1)
                .set(Item::getStatus, 0); // 恢复为"0-待售"

        int rows = itemMapper.update(null, itemUpdateWrapper);

        if (rows == 0) {
            // 如果影响行数为 0，说明商品状态已经被其他业务（如卖家强制下架、后台封禁等）改变
            // 此时不应强行恢复为待售，尊重现有状态即可，只需记录一条日志
            log.warn("商品 {} 库存回滚被阻断：当前状态已非'交易中'，放弃恢复待售状态", itemId);
        }
    }
}