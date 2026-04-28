package com.csust.eco.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
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
                        .contact(new Contact().name("csust-dev")));
    }
}