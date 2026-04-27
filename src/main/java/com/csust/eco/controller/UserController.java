package com.csust.eco.controller;

import com.csust.eco.common.Result;
import com.csust.eco.dto.UserLoginDTO;
import com.csust.eco.dto.UserRegisterDTO;
import com.csust.eco.service.UserService;
import com.csust.eco.vo.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.csust.eco.entity.User;

import java.util.List;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author csust-dev
 * @since 2026-04-16
 */
@RestController
@RequestMapping("api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<String> register(@Validated @RequestBody UserRegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
// 1. 将返回值泛型严格对齐为 UserInfoVO
    public Result<UserInfoVO> login(@Validated @RequestBody UserLoginDTO loginDTO) {
        // 2. 变量名同步修改，做到名副其实 (语义化命名)
        UserInfoVO userInfo = userService.login(loginDTO);
        return Result.success(userInfo);
    }

    // 测试接口: 获取所有用户列表
    @GetMapping("/list")
    public Result<List<User>> list() {
        // 调用 MyBatis-Plus 提供的默认 list() 方法
        List<User> list = userService.list();
        return Result.success(list);
    }
}
