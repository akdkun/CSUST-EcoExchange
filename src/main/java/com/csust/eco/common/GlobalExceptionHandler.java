package com.csust.eco.common;

import cn.dev33.satoken.exception.NotLoginException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

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
        return Result.failed(ResultCode.UNAUTHORIZED);
    }

    /**
     * 2. 精准捕获: Hibernate Validator 参数校验异常 (@Validated 失败时触发)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        // 使用 Stream 流式处理，提取所有错误信息并用逗号拼接
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("参数校验未通过: {}", errorMessage);
        return Result.failed(ResultCode.VALIDATE_FAILED, errorMessage);
    }

    /**
     * 3. 兜底捕获: 系统未知异常 (防止向前端暴露敏感底层堆栈)
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        // 真实的报错堆栈只打印在后端日志中，供开发者排查
        log.error("系统内部发生未知异常: ", e);
        // 统一返回友好的模糊提示语，状态码 500 代表 Server Error
        return Result.failed(ResultCode.FAILED, "系统繁忙，请稍后再试");
    }

    /**
     * 4. 精准捕获: 业务逻辑异常 (BizException)
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        // 业务异常属于正常流程的阻断，不需要打印 error 级别日志吓唬运维，打印 info 或 warn 即可
        log.warn("业务处理被阻断: {}", e.getMessage());
        // 将我们在 Service 层写的错误提示语，原封不动地返回给前端
        return Result.failed(ResultCode.FAILED, e.getMessage());
    }
}