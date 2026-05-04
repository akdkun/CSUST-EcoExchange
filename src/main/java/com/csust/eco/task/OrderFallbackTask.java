package com.csust.eco.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csust.eco.entity.Orders;
import com.csust.eco.mapper.OrdersMapper;
import com.csust.eco.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单兜底定时任务
 * 专治各种因为宕机、重启、网络抖动导致的 Redisson 消息丢失问题
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderFallbackTask {

    private final OrdersMapper ordersMapper;
    private final OrderService orderService;

    /**
     * @Scheduled(cron = "0 0 2 * * ?") 代表每天凌晨 2 点执行一次
     * 这里为了防止白天高峰期占用数据库 IO，我们选择在半夜扫盘。
     * 如果你希望兜底更及时，可以改成 fixedDelay = 1000 * 60 * 30 (每 30 分钟扫一次)
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void clearZombieOrders() {
        log.info("====== 开始执行定时兜底任务：清理超时未支付的僵尸订单 ======");

        // 1. 划定时间边界：当前时间往前推 30 分钟
        // 💡 架构师灵魂考点：为什么是 30 分钟而不是 15 分钟？
        // 因为正常的 Redisson 延迟队列是 15 分钟触发。我们要留出足够的“缓冲期”，
        // 绝对不能抢在 Redisson 之前去取消订单，只去查那些“绝对已经死透了”的异常订单。
        LocalDateTime timeBoundary = LocalDateTime.now().minusMinutes(30);

        // 2. 查出所有状态为 0 (待支付)，且创建时间在 30 分钟以前的异常记录
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getStatus, (byte) 0)
                .le(Orders::getCreateTime, timeBoundary);

        List<Orders> zombieOrders = ordersMapper.selectList(queryWrapper);

        if (zombieOrders.isEmpty()) {
            log.info("兜底任务完成，系统健康，未发现僵尸订单。");
            return;
        }

        log.warn("警报！发现 {} 笔僵尸订单，可能是前期宕机导致，开始强制回滚！", zombieOrders.size());

        // 3. 遍历回滚
        for (Orders order : zombieOrders) {
            try {
                // 直接复用你写好的 Service 层逻辑，那里已经有了完善的库存回滚和状态机保护
                orderService.cancelOrder(order.getId());
                log.info("僵尸订单 {} 强制回滚成功", order.getId());
            } catch (Exception e) {
                log.error("僵尸订单 {} 强制回滚失败，需人工介入: {}", order.getId(), e.getMessage());
            }
        }
    }
}