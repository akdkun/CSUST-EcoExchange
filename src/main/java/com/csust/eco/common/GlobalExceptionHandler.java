package com.csust.eco.common;

import cn.dev33.satoken.exception.NotLoginException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 1. 精准捕获: Sa-Token 未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLoginException(NotLoginException e) {
        log.warn("拦截到未登录请求: {}", e.getMessage());
        // 401 状态码代表 Unauthorized (未授权)
        return Result.error(401, "身份已过期或未登录，请先登录");
    }

    /**
     * 2. 精准捕获: Hibernate Validator 参数校验异常 (@Validated 失败时触发)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        StringBuilder errorMessage = new StringBuilder();

        // 遍历提取出你在 DTO 中写的具体 message (例如: "商品价格不能为空")
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errorMessage.append(fieldError.getDefaultMessage()).append("; ");
        }

        log.warn("参数校验未通过: {}", errorMessage.toString());
        // 400 状态码代表 Bad Request (客户端请求错误)
        return Result.error(400, errorMessage.toString());
    }

    /**
     * 3. 兜底捕获: 系统未知异常 (防止向前端暴露敏感底层堆栈)
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        // 真实的报错堆栈只打印在后端日志中，供开发者排查
        log.error("系统内部发生未知异常: ", e);
        // 统一返回友好的模糊提示语，状态码 500 代表 Server Error
        return Result.error(500, "系统繁忙，请稍后再试");
    }
}