package com.csust.eco.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csust.eco.dto.ItemPublishDTO;
import com.csust.eco.dto.ItemQueryDTO;
import com.csust.eco.entity.Item;
import com.baomidou.mybatisplus.extension.service.IService;
import com.csust.eco.vo.ItemDetailVO;
import com.csust.eco.vo.ItemListVO;

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
    /**
     * 根据id查询二手商品详细信息
     * @param id 商品id
     * @return 查询到的详细数据
     */
    ItemDetailVO getItemDetail(Long id);
    /**
     * 分页查询二手商品
     * @param dto 商品信息集合
     * @return 查询到的简略数据
     */
    Page<ItemListVO> queryItemPage(ItemQueryDTO dto);

}
