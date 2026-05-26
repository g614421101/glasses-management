package com.glasses.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.glasses.util.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public Result<String> handleNotLoginException(NotLoginException e, HttpServletResponse response) {
        response.setStatus(401);
        return Result.error(401, "\u8bf7\u5148\u767b\u5f55");
    }

    @ExceptionHandler({NotRoleException.class, NotPermissionException.class})
    public Result<String> handleForbiddenException(Exception e, HttpServletResponse response) {
        response.setStatus(403);
        return Result.error(403, "\u65e0\u6743\u9650");
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.warn("\u4e1a\u52a1\u5f02\u5e38: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("\u672a\u5904\u7406\u5f02\u5e38: {}", e.getMessage(), e);
        return Result.error(e.getMessage() != null ? e.getMessage() : "\u670d\u52a1\u5668\u5185\u90e8\u9519\u8bef");
    }
}
