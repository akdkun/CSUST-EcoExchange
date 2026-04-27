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
    private String mainImage;
    // 详情页专属：反序列化后的详情图数组
    private List<String> detailImages;

    // 进阶扩展预留：由于是单表设计，前端在详情页需要展示卖家信息，
    // 后续可以在 Service 层通过 sellerId 查询出卖家昵称和头像，组装到这个 VO 里返回。
    private Long sellerId;
    // private String sellerNickname;
    // private String sellerAvatar;

    private Integer status;
    private LocalDateTime createTime;
}