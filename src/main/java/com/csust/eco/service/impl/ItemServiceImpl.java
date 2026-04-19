package com.csust.eco.service.impl;

import cn.dev33.satoken.stp.StpUtil;
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
    @Transactional // 习惯性加上事务注解，保证数据完整性
    public void publish(ItemPublishDTO publishDTO) {

        // 1. 从 Sa-Token 上下文中安全获取当前登录用户的 ID (即卖家 ID)
        long currentUserId = StpUtil.getLoginIdAsLong();

        // 2. 将 DTO 转换为 Entity
        Item item = new Item();
        item.setSellerId(currentUserId);
        item.setTitle(publishDTO.getTitle());
        item.setDescription(publishDTO.getDescription());
        item.setPrice(publishDTO.getPrice());

        // 数据库默认值：stock = 1, status = 0 (待售), 时间自动生成
        // 所以我们不需要手动设置这些字段。

        // 3. 执行入库
        this.save(item);
    }
}