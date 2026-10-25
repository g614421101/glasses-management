package com.glasses.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.glasses.util.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public Result<String> handleNotLoginException(NotLoginException e, HttpServletResponse response) {
        response.setStatus(401);
        return Result.error(401, "请先登录");
    }

    @ExceptionHandler({NotRoleException.class, NotPermissionException.class})
    public Result<String> handleForbiddenException(Exception e, HttpServletResponse response) {
        response.setStatus(403);
        return Result.error(403, "无权限");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("上传文件过大: {}", e.getMessage());
        return Result.error("上传文件过大，超出系统限制（最大允许 100MB）");
    }

    @ExceptionHandler(BizException.class)
    public Result<String> handleBizException(BizException e) {
        // 预期内的业务失败，仅记录 info，不按系统异常告警
        log.info("业务操作失败: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("未处理异常: {}", e.getMessage(), e);
        return Result.error(e.getMessage() != null ? e.getMessage() : "服务器内部错误");
    }
}
