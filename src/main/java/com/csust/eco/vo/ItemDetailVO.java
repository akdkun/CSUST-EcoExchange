package com.csust.eco.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemDetailVO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String mainImage;
    // 关键点：对应数据库中的 JSON 字段，在 Java 中表现为 List
    private List<String> detailImages;
    private LocalDateTime createTime;

    // 业务扩展点：返回卖家 ID，方便前端跳转“联系卖家”或“卖家主页”
    private Long sellerId;

    // 状态映射：0-待售, 1-交易中, 2-已售出, 3-已下架
    private Integer status;
}