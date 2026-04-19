package com.csust.eco.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class UserRegisterDTO {
    @NotBlank(message = "学号不能为空")
    @Size(min = 8, max = 20, message = "学号长度异常")
    private String studentId;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String password;

    @NotBlank(message = "昵称不能为空")
    private String nickname;
}