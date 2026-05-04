package com.csust.eco.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csust.eco.common.BizException;
import com.csust.eco.dto.ItemPublishDTO;
import com.csust.eco.dto.ItemQueryDTO;
import com.csust.eco.entity.Item;
import com.csust.eco.mapper.ItemMapper;
import com.csust.eco.service.ItemService;
import com.csust.eco.vo.ItemDetailVO;
import com.csust.eco.vo.ItemListVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public ItemDetailVO getItemDetail(Long id) {
        // 1. 查询物理实体
        Item item = this.getById(id);

        // 2. 防御性校验：如果商品不存在或已下架，应及时反馈
        if (item == null) {
            throw new BizException("商品已不存在");
        }

        // 3. 转换 VO
        // 注意：由于我们在 Item 实体类上配置了 @TableName(autoResultMap = true)
        // 并对 detail_images 字段使用了 JacksonTypeHandler，
        // MyBatis-Plus 会自动完成从 MySQL JSON 字符串到 List<String> 的反序列化。
        ItemDetailVO detailVO = BeanUtil.copyProperties(item, ItemDetailVO.class);

        return detailVO;
    }

    @Override
    public Page<ItemListVO> queryItemPage(ItemQueryDTO dto) {
        // 1. 构建分页参数
        Page<Item> pageParam = new Page<>(dto.getPageNo(), dto.getPageSize());

        // 2. 组装查询条件
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        // 核心底线: C 端列表只能展示处于待售状态 (0) 的商品
        wrapper.eq(Item::getStatus, 0);

        // 动态 SQL: 模糊查询. (注: LIKE '%keyword%' 会导致索引失效进行全表扫描, 对于初期 V2.0 阶段可接受, 后期可重构为 Elasticsearch)
        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w.like(Item::getTitle, dto.getKeyword())
                    .or()
                    .like(Item::getDescription, dto.getKeyword()));
        }

        // 默认按发布时间倒序排列, 保证最新发布的在最前
        wrapper.orderByDesc(Item::getCreateTime);

        // 3. 执行物理分页查询
        Page<Item> itemPage = this.page(pageParam, wrapper);

        // 4. 将持久层的 Entity 转换为隔离层的 VO
        List<ItemListVO> voList = itemPage.getRecords().stream()
                .map(item -> BeanUtil.copyProperties(item, ItemListVO.class))
                .collect(Collectors.toList());

        // 5. 重新组装 Page 对象并返回
        Page<ItemListVO> resultPage = new Page<>(itemPage.getCurrent(), itemPage.getSize(), itemPage.getTotal());
        resultPage.setRecords(voList);

        return resultPage;
    }
}