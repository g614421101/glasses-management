package com.glasses.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glasses.service.OperationLogService;
import com.glasses.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 操作日志切面：拦截 controller 包内所有写操作（非 GET），
 * 提取操作人/模块/动作/参数/结果/耗时/IP 并交由 OperationLogService 落库。
 * 日志记录失败不影响业务。
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    /** 参数 JSON 最大长度，超出截断 */
    private static final int MAX_PARAMS_LENGTH = 2000;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Around("execution(public * com.glasses.controller..*.*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return pjp.proceed();
        }
        HttpServletRequest request = attributes.getRequest();
        String method = request.getMethod();
        // 只记录写操作，查询不记录
        if ("GET".equalsIgnoreCase(method)) {
            return pjp.proceed();
        }

        String uri = request.getRequestURI();
        // 认证类接口（登录/注册/改密/个人资料）不属于业务增删改，不记录
        if (uri.startsWith("/api/auth/")) {
            return pjp.proceed();
        }
        // 操作日志类接口（手动清理等）不记录，避免审计闭环自引用
        if (uri.startsWith("/api/operation-log/")) {
            return pjp.proceed();
        }

        long start = System.currentTimeMillis();
        Integer status = null;
        String message = null;
        try {
            Object result = pjp.proceed();
            if (result instanceof Result) {
                Result<?> r = (Result<?>) result;
                status = r.getCode();
                message = r.getMsg();
            } else {
                status = 200;
            }
            return result;
        } catch (Throwable e) {
            status = 500;
            message = e.getMessage();
            throw e;
        } finally {
            long costMs = System.currentTimeMillis() - start;
            operationLogService.recordLog(resolveAction(method), method, uri,
                    serializeArgs(pjp.getArgs()), status, message, costMs, request.getRemoteAddr());
        }
    }

    private String resolveAction(String httpMethod) {
        switch (httpMethod.toUpperCase()) {
            case "POST":
                return "ADD";
            case "PUT":
            case "PATCH":
                return "UPDATE";
            case "DELETE":
                return "DELETE";
            default:
                return "OTHER";
        }
    }

    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null || isSkippable(arg)) {
                continue;
            }
            map.put("arg" + i, arg);
        }
        if (map.isEmpty()) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(map);
            return json.length() > MAX_PARAMS_LENGTH ? json.substring(0, MAX_PARAMS_LENGTH) : json;
        } catch (Exception e) {
            log.debug("操作日志参数序列化失败: {}", e.getMessage());
            return null;
        }
    }

    private boolean isSkippable(Object arg) {
        return arg instanceof HttpServletRequest || arg instanceof HttpServletResponse
                || arg instanceof MultipartFile || arg instanceof MultipartHttpServletRequest
                || arg instanceof BindingResult;
    }
}
