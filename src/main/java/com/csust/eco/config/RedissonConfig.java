package com.csust.eco.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setConnectionMinimumIdleSize(10)
                .setConnectionPoolSize(50);

        // 设置全局序列化器为 Jackson, 保证数据可读性
        config.setCodec(new org.redisson.codec.JsonJacksonCodec());

        return Redisson.create(config);
    }
}