package com.csust.eco.service;

import com.csust.eco.dto.UserLoginDTO;
import com.csust.eco.dto.UserRegisterDTO;
import com.csust.eco.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author csust-dev
 * @since 2026-04-16
 */
public interface UserService extends IService<User> {
    void register(UserRegisterDTO registerDTO);
    String login(UserLoginDTO loginDTO);
}
