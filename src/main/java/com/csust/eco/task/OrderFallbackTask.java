package com.csust.eco.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csust.eco.entity.Orders;
import com.csust.eco.mapper.OrdersMapper;
import com.csust.eco.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderFallbackTask {

    private final OrdersMapper ordersMapper;
    private final OrderService orderService;
    private final RedissonClient redissonClient; // 引入 Redisson 解决分布式调度问题

    @Scheduled(cron = "0 0 2 * * ?")
    public void clearZombieOrders() {
        // 利用分布式锁保证集群环境下只有一个节点执行扫表
        String lockKey = "eco:task:lock:zombie_orders";
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁，等待 0 秒，任务锁 10 分钟后自动释放防止死锁
            if (!lock.tryLock(0, 10, TimeUnit.MINUTES)) {
                log.info("兜底任务已在集群其他节点执行，当前节点跳过.");
                return;
            }

            log.info("====== 节点获取分布式锁成功，开始执行定时兜底任务 ======");
            LocalDateTime timeBoundary = LocalDateTime.now().minusMinutes(30);

            // 分批处理逻辑：每次最多查 500 条，直到处理完毕
            int batchSize = 500;
            while (true) {
                LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Orders::getStatus, (byte) 0)
                        .le(Orders::getCreateTime, timeBoundary)
                        // 核心修复 2：物理级别限制单次拉取数量，彻底杜绝 OOM 风险
                        .last("LIMIT " + batchSize);

                List<Orders> zombieOrders = ordersMapper.selectList(queryWrapper);

                if (zombieOrders.isEmpty()) {
                    log.info("兜底任务处理完成，无更多僵尸订单.");
                    break; // 退出循环
                }

                log.warn("发现 {} 笔僵尸订单，开始分批强制回滚！", zombieOrders.size());

                for (Orders order : zombieOrders) {
                    try {
                        orderService.cancelOrderSystem(order.getId());
                    } catch (Exception e) {
                        log.error("订单 {} 强制回滚失败: {}", order.getId(), e.getMessage());
                    }
                }

                // 如果本次查出的数据少于 batchSize，说明已经是最后一批了，直接退出
                if (zombieOrders.size() < batchSize) {
                    break;
                }
            }

        } catch (InterruptedException e) {
            log.warn("兜底任务线程被中断", e);
            Thread.currentThread().interrupt();
        } finally {
            // 确保任务执行完毕后释放分布式锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}