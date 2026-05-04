package com.csust.eco.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充字段拦截器
 */
@Slf4j
@Component
public class MybatisPlusFillHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入时的公共字段自动填充...");
        // 自动填充创建时间和更新时间 (注意这里的字段名是 Java 实体类里的属性名，不是数据库表字段名)
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // 可选：如果你之前没设置数据库 DEFAULT 0，也可以在这里自动填充 isDeleted = 0
        // this.strictInsertFill(metaObject, "isDeleted", Byte.class, (byte) 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新时的公共字段自动填充...");
        // 更新时，只自动修改更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}