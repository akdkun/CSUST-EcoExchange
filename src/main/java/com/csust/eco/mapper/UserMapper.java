package com.csust.eco.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csust.eco.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author csust-dev
 * @since 2026-04-16
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
