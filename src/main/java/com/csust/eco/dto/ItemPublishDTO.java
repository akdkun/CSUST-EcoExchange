package com.csust.eco.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ItemPublishDTO {

    @NotBlank(message = "商品标题不能为空")
    private String title;

    private String description; // 描述可以为空

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    // 注意：目前技术栈基准清单中提到，图片上传功能将通过 MinIO/OSS 实现，
    // 所以这里的 DTO 暂时不包含图片 URL 字段。我们先实现核心的纯文本发布逻辑。
}