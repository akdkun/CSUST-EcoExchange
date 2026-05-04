package com.csust.eco.common;

import lombok.Getter;

/**
 * 自定义业务异常
 * 用于在 Service 层抛出已知的业务逻辑错误（如：库存不足、商品下架、密码错误等）
 */
@Getter
public class BizException extends RuntimeException {
    private final Integer code;

    public BizException(String message) {
        super(message);
        this.code = ResultCode.FAILED.getCode(); // 默认 500
    }

    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }
}