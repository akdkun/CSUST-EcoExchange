package com.csust.eco.service;

import com.csust.eco.dto.ItemPublishDTO;
import com.csust.eco.entity.Item;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 二手商品表 服务类
 * </p>
 *
 * @author csust-dev
 * @since 2026-04-16
 */
public interface ItemService extends IService<Item> {
    /**
     * 发布二手商品
     * @param publishDTO 商品信息传输对象
     * @param sellerId   卖家ID (由 Controller 层安全提取)
     * @return 插入成功后的商品主键ID
     */
    Long publish(ItemPublishDTO publishDTO, Long sellerId);
}
