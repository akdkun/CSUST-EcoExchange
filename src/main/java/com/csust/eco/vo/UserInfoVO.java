package com.csust.eco.vo;

import lombok.Data;

@Data
public class UserInfoVO {
    private Long id;
    private String studentId; // 脱敏展示或原样返回，视业务而定
    private String nickname;
    private String avatar;
    // 登录成功时，顺便将 Sa-Token 的值包裹在 VO 中返回给前端
    private String tokenValue;
}