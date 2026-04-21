package com.csust.eco.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCreateDTO {
    // 零信任原则: 只接收物理主键, 拒绝接收价格和买家ID
    @NotNull(message = "商品ID不能为空")
    private Long itemId;
}