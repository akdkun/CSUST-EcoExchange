package com.csust.eco.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.csust.eco.common.Result;
import com.csust.eco.dto.OrderCreateDTO;
import com.csust.eco.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 交易订单表 前端控制器
 * </p>
 *
 * @author csust-dev
 * @since 2026-04-16
 */
@Tag(name = "3. 交易与订单模块", description = "核心交易引擎，包含高并发防超卖与延迟队列逻辑")
@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "创建订单 (下单)", description = "[需登录] 买家发起抢购请求. 系统采用 Redisson 分布式锁确保单件商品不会被重复售卖(防超卖),下单成功后返回订单物理主键 ID, 并自动抛入延迟队列进行 15 分钟倒计时.")
    @PostMapping("/create")
    public Result<Long> createOrder(@Validated @RequestBody OrderCreateDTO createDTO) {

        // 1. 安全防线：提取当前登录买家的 ID，绝不允许前端伪造买家身份
        long buyerId = StpUtil.getLoginIdAsLong();

        // 2. 将纯净的数据和提取出的身份交给 Service 层处理
        Long orderId = orderService.createOrder(createDTO, buyerId);

        return Result.success(orderId);
    }
}