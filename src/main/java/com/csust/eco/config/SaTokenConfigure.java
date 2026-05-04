package com.csust.eco.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {

            // 1. 获取当前请求的上下文对象
            SaRequest req = SaHolder.getRequest();

            // 2. 动态防线：如果是获取商品详情的 GET 请求 (路径格式如 /api/item/7)，则直接放行
            if ("GET".equals(req.getMethod()) && req.getRequestPath().matches("/api/item/\\d+")) {
                return; // 提前 return，跳过后续所有拦截检查
            }

            // 3. 静态白名单与全局兜底拦截
            SaRouter.match("/**")
                    .notMatch(
                            "/api/user/login",
                            "/api/user/register",
                            "/api/item/page",
                            "/favicon.ico",
                            "/doc.html",
                            "/webjars/**",
                            "/v3/api-docs/**",
                            "/swagger-ui/**"
                    )
                    // 剩下的所有请求，执行严格的登录拦截
                    .check(r -> StpUtil.checkLogin());

        })).addPathPatterns("/**"); // 拦截器拦截所有路径，具体鉴权逻辑交由内部的 handle 处理
    }
}