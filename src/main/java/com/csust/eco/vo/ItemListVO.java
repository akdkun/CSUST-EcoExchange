package com.csust.eco.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ItemListVO {
    private Long id;
    private String title;
    private BigDecimal price;
    private String mainImage; // 列表页只需主图, 不返回 detail_images 这个庞大的 JSON 数组
    private LocalDateTime createTime;
}