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

    // 核心队列名称约束
    private static final String QUEUE_NAME = "eco:queue:order:cancel";

    /**
     * ApplicationRunner 会在 Spring Boot 核心容器初始化完毕后自动执行。
     * 我们在这里启动一个永不停止的物理后台线程 (Daemon Thread)。
     */
    @Override
    public void run(ApplicationArguments args) {
        new Thread(() -> {
            log.info("====== 订单超时监听后台线程已启动 ======");
            // 1. 获取阻塞队列（这是数据最终掉落的地方）
            RBlockingQueue<Long> blockingQueue = redissonClient.getBlockingQueue(QUEUE_NAME);

            while (true) {
                try {
                    // 2. take() 方法是阻塞的！
                    // 如果队列为空，这个线程会在这里安静地休眠，不会消耗任何 CPU 资源。
                    // 一旦 Redis 把超时的订单 ID 投递过来，它会瞬间苏醒。
                    Long expiredOrderId = blockingQueue.take();

                    log.info("捕获到超时订单: {}, 准备执行回滚策略", expiredOrderId);

                    // 3. 呼叫 Service 层执行跨表事务
                    try {
                        orderService.cancelOrder(expiredOrderId);
                    } catch (Exception e) {
                        log.error("订单 {} 取消失败，引发严重物理异常: {}", expiredOrderId, e.getMessage());
                        // 生产环境中，这里通常会将失败的订单丢入死信队列 (DLQ) 进行人工补偿
                    }

                } catch (InterruptedException e) {
                    log.warn("订单监听线程被中断");
                    Thread.currentThread().interrupt();
                    break;
                } catch (org.redisson.RedissonShutdownException e) {
                    // 核心修复：捕获 Redisson 关机异常，体面退出死循环
                    log.info("检测到系统正在关闭，Redisson 客户端已断开，停止监听超时队列。");
                    break;
                } catch (Exception e) {
                    log.error("监听队列发生未知异常: {}", e.getMessage());
                }
            }
        }, "Order-Timeout-Listener-Thread").start();
    }
}