package com.csust.eco.listener;

import com.csust.eco.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutListener implements ApplicationRunner {

    private final RedissonClient redissonClient;
    private final OrderService orderService;
    private static final String QUEUE_NAME = "eco:queue:order:cancel";

    // 引入线程池：根据 IO 密集型特性，分配多个工作线程并发处理超时订单
    private final ExecutorService executorService = Executors.newFixedThreadPool(
            5,
            r -> {
                Thread t = new Thread(r, "Order-Timeout-Worker-" + new AtomicInteger().incrementAndGet());
                t.setDaemon(true); // 同样保持守护线程特性
                return t;
            }
    );

    @Override
    public void run(ApplicationArguments args) {
        log.info("====== 订单超时监听多线程引擎已启动 ======");
        RBlockingQueue<Long> blockingQueue = redissonClient.getBlockingQueue(QUEUE_NAME);

        // 核心修复 1: 启动多个消费者并发争抢队列，极大提升库存回滚的吞吐量
        for (int i = 0; i < 5; i++) {
            executorService.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        // 阻塞获取，多线程安全争抢
                        Long expiredOrderId = blockingQueue.take();
                        log.info("线程 {} 捕获到超时订单: {}, 准备执行回滚", Thread.currentThread().getName(), expiredOrderId);

                        try {
                            // 注意：这里的 cancelOrder 是系统级调用，不需要传递 userId 校验越权
                            orderService.cancelOrderSystem(expiredOrderId);
                        } catch (Exception e) {
                            log.error("订单 {} 取消失败: {}", expiredOrderId, e.getMessage());

                            // 消息可靠性保障。失败后重新放回延迟队列，延迟 10 秒后重试，避免瞬间死循环压垮 DB
                            RDelayedQueue<Long> delayedQueue = redissonClient.getDelayedQueue(blockingQueue);
                            delayedQueue.offer(expiredOrderId, 10, TimeUnit.SECONDS);
                            log.info("订单 {} 已重新抛入延迟队列，等待重试", expiredOrderId);
                        }

                    } catch (InterruptedException e) {
                        log.warn("订单监听线程被中断");
                        Thread.currentThread().interrupt();
                        break;
                    } catch (org.redisson.RedissonShutdownException e) {
                        log.info("检测到系统正在关闭，Redisson 已断开。");
                        break;
                    } catch (Exception e) {
                        log.error("监听队列发生未知异常: ", e);
                    }
                }
            });
        }
    }

    // 优雅停机：确保 Spring 容器关闭时，线程池能被正确关闭
    @PreDestroy
    public void destroy() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}