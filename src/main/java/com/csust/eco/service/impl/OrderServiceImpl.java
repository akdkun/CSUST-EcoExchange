package com.csust.eco.service.impl;

import cn.hutool.core.util.IdUtil;
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
                throw new RuntimeException("当前商品抢购人数过多, 请重试");
            }

            // 获取 AOP 代理对象, 保护底层事务
            OrderService proxy = (OrderService) AopContext.currentProxy();
            return proxy.createOrderInTx(itemId, buyerId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("系统繁忙, 下单失败");
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

        // 乐观锁扣减库存
        item.setStock(0);
        item.setStatus((byte) 1);
        int updatedRows = itemMapper.updateById(item);
        if (updatedRows == 0) {
            throw new BizException("扣减库存失败");
        }

        Orders order = new Orders();
        order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        order.setBuyerId(buyerId);
        order.setItemId(itemId);
        order.setAmount(item.getPrice());
        order.setStatus((byte) 0);
        order.setCreateTime(LocalDateTime.now());

        this.save(order);

        // --- V2.0 新增: 将订单抛入延迟队列 ---
        org.redisson.api.RBlockingQueue<Long> blockingQueue = redissonClient.getBlockingQueue("eco:queue:order:cancel");
        org.redisson.api.RDelayedQueue<Long> delayedQueue = redissonClient.getDelayedQueue(blockingQueue);

        // 物理逻辑：告诉 Redis，15 分钟后，把这个 order.getId() 从 delayedQueue 转移到 blockingQueue 中去
        delayedQueue.offer(order.getId(), 15, TimeUnit.MINUTES);
        log.info("订单 {} 已加入延迟队列, 15分钟后若未支付将自动回滚", order.getId());

        return order.getId();
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancelOrder(Long orderId) {
        // 1. 查询订单当前真实状态
        Orders order = this.getById(orderId);
        if (order == null) {
            return;
        }

        // 2. 幂等性防御：只有在 "0-待支付" 状态下才能取消
        // 如果买家在第 14 分钟付款了，状态变成了 1，这里就直接放行，不做处理
        if (order.getStatus() != 0) {
            log.info("订单 {} 状态非待支付 (当前状态:{}), 无需取消", orderId, order.getStatus());
            return;
        }

        // 3. 物理回滚：恢复商品状态和库存
        Item item = itemMapper.selectById(order.getItemId());
        if (item != null) {
            item.setStock(1);
            item.setStatus((byte) 0); // 0-待售
            itemMapper.updateById(item);
            log.info("订单 {} 超时未支付, 商品 {} 库存已物理回滚", orderId, item.getId());
        }

        // 4. 更新订单状态为 "4-已取消"
        order.setStatus((byte) 4);
        this.updateById(order);
    }
}