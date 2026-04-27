package com.csust.eco.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code; // 状态码: 200 成功, 500 失败等
    private String message; // 提示信息
    private T data; // 实际承载的数据

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        // 400 代表 Bad Request, 适合用于参数校验未通过的场景
        result.setCode(400);
        result.setMessage(message);
        return result;
    }
}