package com.csust.eco.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，定义路由拦截规则
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**") // 拦截所有接口请求
                .excludePathPatterns(   // 配置白名单 (不需要登录就能访问的接口)
                        "/api/user/login",
                        "/api/user/register",
                        "/api/item/page",
                        "/api/item/*",
                        "/favicon.ico",  // 排除浏览器默认图标请求
                        "/doc.html",     // 如果后续集成 Knife4j，需要排除接口文档路由
                        "/webjars/**",
                        "/v3/api-docs/**"
                );
    }
}