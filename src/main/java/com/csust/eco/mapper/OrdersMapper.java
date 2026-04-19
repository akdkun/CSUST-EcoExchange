package com.csust.eco.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csust.eco.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 交易订单表 Mapper 接口
 * </p>
 *
 * @author csust-dev
 * @since 2026-04-16
 */
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

}
