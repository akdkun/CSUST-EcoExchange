package com.csust.eco.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "商品发布表单入参")
public class ItemPublishDTO {

    @Schema(description = "商品标题", example = "九成新 HHKB 机械键盘", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "商品标题不能为空")
    private String title;

    @Schema(description = "商品详情描述", example = "自用半年, 无暗病, 包装配件齐全, 长理云影校区支持面交.")
    private String description; // 描述可以为空

    @Schema(description = "商品价格", example = "1500.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    @Schema(description = "商品主图URL (建议调用图片上传接口获取)", example = "http://127.0.0.1:9000/eco-exchange/2026/04/28/keyboard_main.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "商品主图不能为空, 请先上传图片")
    private String mainImage;

    @Schema(description = "商品详情图URL数组 (最多允许9张)", example = "[\"http://127.0.0.1:9000/eco-exchange/2026/04/28/detail1.jpg\", \"http://127.0.0.1:9000/eco-exchange/2026/04/28/detail2.jpg\"]")
    @Size(max = 9, message = "详情图最多只能上传9张")
    private List<String> detailImages;
}