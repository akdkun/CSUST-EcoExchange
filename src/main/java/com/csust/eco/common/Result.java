package com.csust.eco.common;

import lombok.Data;
import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private Integer code;
    private String message;
    private T data;
    private long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    // 成功：无数据返回
    public static <T> Result<T> success() {
        return success(null);
    }

    // 成功：有数据返回
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    // 失败：使用枚举
    public static <T> Result<T> failed(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        return result;
    }

    // 失败：自定义消息 (常用语参数校验失败)
    public static <T> Result<T> failed(String message) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.FAILED.getCode());
        result.setMessage(message);
        return result;
    }

    // 失败: 指定业务码 + 自定义提示信息
    public static <T> Result<T> failed(ResultCode resultCode, String customMessage) {
        Result<T> result = new Result<>();
        // 提取枚举里的 400 状态码
        result.setCode(resultCode.getCode());
        // 覆盖为传进来的动态校验报错
        result.setMessage(customMessage);
        return result;
    }
}