package com.csust.eco.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建订单入参")
public class OrderCreateDTO {

    // 零信任原则: 只接收物理主键, 拒绝接收价格和买家ID
    @Schema(description = "要购买的二手商品唯一主键 ID", example = "7", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品ID不能为空")
    private Long itemId;
}