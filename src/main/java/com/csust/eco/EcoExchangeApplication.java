package com.csust.eco;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.csust.eco.mapper") // 核心: 指定 Mapper 接口所在的包
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableScheduling
public class EcoExchangeApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcoExchangeApplication.class, args);
	}

}
