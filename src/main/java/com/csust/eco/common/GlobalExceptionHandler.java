package com.csust.eco.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 捕获所有 Exception 类的异常
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统异常: ", e);
        // 实际项目中应细化异常捕获类型 (如参数校验异常, 业务自定义异常等)
        return Result.error(500, "服务器内部异常: " + e.getMessage());
    }
}