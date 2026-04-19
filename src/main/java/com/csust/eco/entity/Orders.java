package com.csust.eco.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 交易订单表
 * </p>
 *
 * @author csust-dev
 * @since 2026-04-16
 */
@Getter
@Setter
@ToString
@TableName("orders")
public class Orders implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 业务订单号(防并发)
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 买家ID
     */
    @TableField("buyer_id")
    private Long buyerId;

    /**
     * 商品ID
     */
    @TableField("item_id")
    private Long itemId;

    /**
     * 实付金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 状态: 0-待支付, 1-已支付, 2-已发货, 3-已完成, 4-已取消
     */
    @TableField("status")
    private Byte status;

    /**
     * 创单时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
