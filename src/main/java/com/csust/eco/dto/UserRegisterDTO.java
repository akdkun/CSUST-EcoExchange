package com.csust.eco.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Schema(description = "用户注册表单入参")
public class UserRegisterDTO {

    @Schema(description = "学号 (长度 8-20 位)", example = "2021000101", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "学号不能为空")
    @Size(min = 8, max = 20, message = "学号长度异常")
    private String studentId;

    @Schema(description = "登录密码 (长度 6-20 位)", example = "csust123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String password;

    @Schema(description = "用户昵称", example = "长理淘金客", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "昵称不能为空")
    private String nickname;
}