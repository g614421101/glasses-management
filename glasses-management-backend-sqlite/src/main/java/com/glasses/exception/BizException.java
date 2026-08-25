package com.glasses.exception;

/**
 * 业务异常：由 Service 层抛出，GlobalExceptionHandler 统一转为 Result.error(msg)。
 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }
}
