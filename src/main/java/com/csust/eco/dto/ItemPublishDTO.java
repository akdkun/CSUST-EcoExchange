package com.csust.eco.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ItemPublishDTO {

    @NotBlank(message = "商品标题不能为空")
    private String title;

    private String description; // 描述可以为空

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    @NotBlank(message = "商品主图不能为空, 请先上传图片")
    private String mainImage;
    
    @Size(max = 9, message = "详情图最多只能上传9张")
    private List<String> detailImages;
}