package com.csust.eco.controller;

import com.csust.eco.common.Result;
import com.csust.eco.dto.UserLoginDTO;
import com.csust.eco.service.UserService;
import com.csust.eco.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "0. 身份认证模块")
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {
    final private UserService userService;

    @Operation(summary = "用户登录")
    @PostMapping // 语义: 往服务器端提交数据, 创建一个属于当前用户的会话凭证
    public Result<UserInfoVO> login(@Validated @RequestBody UserLoginDTO loginDTO) {
        return Result.success(userService.login(loginDTO));
    }
}
