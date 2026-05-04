package com.csust.eco.listener;

import com.csust.eco.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutListener implements ApplicationRunner {

    private final RedissonClient redissonClient;
    private final OrderService orderService;
    private static final String QUEUE_NAME = "eco:queue:order:cancel";

    @Override
    public void run(ApplicationArguments args) {
        Thread listenerThread = new Thread(() -> {
            log.info("====== 订单超时监听后台线程已启动 ======");
            RBlockingQueue<Long> blockingQueue = redissonClient.getBlockingQueue(QUEUE_NAME);

            while (true) {
                try {
                    // 阻塞获取，零 CPU 损耗
                    Long expiredOrderId = blockingQueue.take();
                    log.info("捕获到超时订单: {}, 准备执行回滚策略", expiredOrderId);

                    try {
                        orderService.cancelOrder(expiredOrderId);
                    } catch (Exception e) {
                        log.error("订单 {} 取消失败，引发严重物理异常: {}", expiredOrderId, e.getMessage());
                        // 补偿方案：生产环境中可重试入队，或依赖 @Scheduled 兜底定时任务扫描数据库
                    }

                } catch (InterruptedException e) {
                    log.warn("订单监听线程被中断");
                    Thread.currentThread().interrupt();
                    break;
                } catch (org.redisson.RedissonShutdownException e) {
                    log.info("检测到系统正在关闭，Redisson 客户端已断开，停止监听超时队列。");
                    break;
                } catch (Exception e) {
                    log.error("监听队列发生未知异常: {}", e.getMessage());
                }
            }
        });

        // 核心修复 1: 赋予规范的线程名，方便线上排查日志和使用 jstack 分析
        listenerThread.setName("Order-Timeout-Listener-Thread");
        // 核心修复 2: 设置为守护线程 (Daemon Thread)。当 Spring 的主线程退出时，守护线程会随 JVM 立刻强行销毁，不阻碍停机。
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
}