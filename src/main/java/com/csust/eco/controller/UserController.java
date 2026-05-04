package com.csust.eco.controller;

import cn.hutool.core.bean.BeanUtil;
import com.csust.eco.common.Result;
import com.csust.eco.dto.UserLoginDTO;
import com.csust.eco.dto.UserRegisterDTO;
import com.csust.eco.entity.User;
import com.csust.eco.service.UserService;
import com.csust.eco.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author csust-dev
 * @since 2026-04-16
 */
@Tag(name = "1. 用户认证模块", description = "提供用户注册、登录、会话获取及基础信息管理")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    final private UserService userService;

    @Operation(summary = "用户注册", description = "[游客可用]校验学号唯一性, 使用 MD5 加密存储密码")
    @PostMapping("/register")
    public Result<String> register(@Validated @RequestBody UserRegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success("注册成功");
    }

    @Operation(summary = "用户登录", description = "[游客可用]学号密码验证, 成功后下发 Sa-Token 凭证及用户基础脱敏信息")
    @PostMapping("/login")
    public Result<UserInfoVO> login(@Validated @RequestBody UserLoginDTO loginDTO) {
        UserInfoVO userInfo = userService.login(loginDTO);
        return Result.success(userInfo);
    }
}