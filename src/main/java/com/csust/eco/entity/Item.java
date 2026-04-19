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
 * 二手商品表
 * </p>
 *
 * @author csust-dev
 * @since 2026-04-16
 */
@Getter
@Setter
@ToString
@TableName("item")
public class Item implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 卖家用户ID
     */
    @TableField("seller_id")
    private Long sellerId;

    /**
     * 商品标题
     */
    @TableField("title")
    private String title;

    /**
     * 商品详情描述
     */
    @TableField("description")
    private String description;

    /**
     * 价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 库存(二手通常为1)
     */
    @TableField("stock")
    private Integer stock;

    /**
     * 状态: 0-待售, 1-交易中, 2-已售出, 3-已下架
     */
    @TableField("status")
    private Byte status;

    /**
     * 发布时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
