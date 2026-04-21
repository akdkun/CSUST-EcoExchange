package com.csust.eco.service;

import com.csust.eco.dto.OrderCreateDTO;
import com.csust.eco.entity.Orders;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 交易订单表 服务类
 * </p>
 *
 * @author csust-dev
 * @since 2026-04-16
 */

public interface OrderService extends IService<Orders> {
    /**
     * 第一层: 并发控制层 (负责调度 Redisson 分布式锁)
     */
    Long createOrder(OrderCreateDTO dto, Long buyerId);

    /**
     * 第二层: 物理事务层 (负责落盘, 被 AOP 代理调用)
     */
    Long createOrderInTx(Long itemId, Long buyerId);

    /**
     * 超时自动取消订单并回滚库存
     */
    void cancelOrder(Long orderId);
}