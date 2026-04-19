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
    void publish(ItemPublishDTO publishDTO);
}
