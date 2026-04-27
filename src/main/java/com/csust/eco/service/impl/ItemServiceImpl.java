package com.csust.eco.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.csust.eco.dto.ItemPublishDTO;
import com.csust.eco.entity.Item;
import com.csust.eco.mapper.ItemMapper;
import com.csust.eco.service.ItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publish(ItemPublishDTO publishDTO, Long sellerId) { // 接收外部传入的卖家 ID

        // 将 DTO 转换为 Entity
        Item item = new Item();

        // 直接使用外部传入的已鉴权身份
        item.setSellerId(sellerId);

        item.setTitle(publishDTO.getTitle());
        item.setDescription(publishDTO.getDescription());
        item.setPrice(publishDTO.getPrice());

        // 映射图片字段
        item.setMainImage(publishDTO.getMainImage());
        item.setDetailImages(publishDTO.getDetailImages());

        item.setStock(1);
        item.setStatus((byte) 0);

        // 执行入库
        this.save(item);

        return item.getId();
    }
}