package com.csust.eco.controller;

import com.csust.eco.common.Result;
import com.csust.eco.dto.UserRegisterDTO;
import com.csust.eco.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author csust-dev
 * @since 2026-04-16
 */
@Tag(name = "1. 用户管理模块", description = "提供用户注册功能")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    final private UserService userService;

    @Operation(summary = "用户注册", description = "[游客可用]校验学号唯一性, 使用 MD5 加密存储密码")
    @PostMapping
    public Result<String> register(@Validated @RequestBody UserRegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success("注册成功");
    }
}

