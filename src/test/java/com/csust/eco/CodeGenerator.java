package com.csust.eco;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

public class CodeGenerator {

    public static void main(String[] args) {
        // 1. 数据库连接配置 (彻底避开 root 权限解析的暗坑)
        String url = "jdbc:mysql://127.0.0.1:3306/csust_eco_exchange?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
        String username = "root";
        String password = "hyk050831";

        // 2. 获取项目根目录，用于定位生成的代码存放位置
        String projectPath = System.getProperty("user.dir");

        FastAutoGenerator.create(url, username, password)
                // 全局配置
                .globalConfig(builder -> {
                    builder.author("csust-dev") // 设置作者
                            .disableOpenDir() // 生成后不自动打开资源管理器
                            .outputDir(projectPath + "/src/main/java"); // 指定输出目录
                })
                // 包配置
                .packageConfig(builder -> {
                    builder.parent("com.csust.eco") // 父包名
                            // 设置 Mapper XML 文件的输出路径 (通常放在 src/main/resources/mapper 下)
                            .pathInfo(Collections.singletonMap(OutputFile.xml, projectPath + "/src/main/resources/mapper"));
                })
                // 策略配置 (核心映射逻辑)
                .strategyConfig(builder -> {
                    builder.addInclude("user", "item", "orders") // 指定需要生成的表名
                            // Entity (实体类) 策略
                            .entityBuilder()
                            .enableLombok() // 开启 Lombok 自动生成 getter/setter/toString
                            .enableTableFieldAnnotation() // 自动生成 @TableField 等注解，避免保留字冲突
                            // Controller (控制层) 策略
                            .controllerBuilder()
                            .enableRestStyle() // 开启 @RestController 风格，返回 JSON 格式数据
                            // Service (服务层) 策略
                            .serviceBuilder()
                            .formatServiceFileName("%sService") // 格式化 Service 接口名 (去除默认的 I 前缀，如 IUserService 变为 UserService)
                            // Mapper (持久层) 策略
                            .mapperBuilder()
                            .enableMapperAnnotation(); // 开启 @Mapper 注解
                })
                // 使用 Freemarker 模板引擎 (与你的 pom.xml 依赖呼应)
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}