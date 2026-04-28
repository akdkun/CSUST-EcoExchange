package com.csust.eco.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CSUST-EcoExchange 校园二手交易平台 API")
                        .version("V2.0")
                        .description("底层环境: JDK 21 + Spring Boot 3.2 + MyBatis-Plus")
                        .contact(new Contact().name("csust-dev")))
                // 1. 注册全局安全策略定义 (告诉 Swagger 我们用什么方式鉴权)
                .components(new Components()
                        .addSecuritySchemes("satoken", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY) // API Key 模式
                                .in(SecurityScheme.In.HEADER)     // 参数放在 Header 中
                                .name("satoken")                  // Sa-Token 默认的请求头名字
                        ))
                // 2. 将安全策略应用到所有的接口上
                .addSecurityItem(new SecurityRequirement().addList("satoken"));
    }
}