package com.csust.eco.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csust.eco.entity.Item;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 二手商品表 Mapper 接口
 * </p>
 *
 * @author csust-dev
 * @since 2026-04-16
 */
@Mapper
public interface ItemMapper extends BaseMapper<Item> {

}
