package com.glasses.config;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * HTTP 请求日志过滤器。
 * <p>
 * 记录每个 API 请求的方法、URI、登录用户、响应状态码和耗时，
 * 排除静态资源、健康检查和 OPTIONS 预检请求。
 */
@Slf4j
@Component
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        // 排除非 API 请求
        if (!uri.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String method = request.getMethod();
            int status = response.getStatus();
            String userInfo = getUserInfo();

            if (status >= 400) {
                log.warn("{} {} | user={} | {} | {}ms", method, uri, userInfo, status, duration);
            } else {
                log.info("{} {} | user={} | {} | {}ms", method, uri, userInfo, status, duration);
            }
        }
    }

    private String getUserInfo() {
        try {
            if (StpUtil.isLogin()) {
                String username = StpUtil.getSession().getString("username");
                return username + "(" + StpUtil.getLoginIdAsLong() + ")";
            }
        } catch (Exception ignored) {
            // 未登录或会话已过期
        }
        return "anonymous";
    }
}
