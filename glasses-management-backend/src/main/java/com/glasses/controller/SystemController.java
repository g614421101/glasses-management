package com.glasses.controller;

import com.glasses.dto.SetupDTO;
import com.glasses.service.AuthService;
import com.glasses.util.LanIpUtil;
import com.glasses.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    @Value("${server.port:8080}")
    private int port;

    @Autowired
    private AuthService authService;

    @GetMapping("/lan-info")
    public Result<Map<String, Object>> getLanInfo() {
        String ip = LanIpUtil.findLanIp();
        if (ip == null) {
            return Result.error("未检测到局域网连接");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("ip", ip);
        data.put("port", port);
        return Result.success(data);
    }

    /**
     * 初始化状态查询（免认证）：登录页据此决定展示登录表单还是初始化表单。
     */
    @GetMapping("/setup-status")
    public Result<Map<String, Object>> setupStatus() {
        Map<String, Object> data = new HashMap<>();
        data.put("initialized", authService.isInitialized());
        return Result.success(data);
    }

    /**
     * 首次初始化：创建管理员（免认证，仅未初始化时可调用，需邀请码，创建后自动关闭）。
     */
    @PostMapping("/setup")
    public Result<String> setup(@RequestBody SetupDTO dto) {
        authService.setupAdmin(dto);
        return Result.success("初始化成功，请使用新账号登录");
    }
}
