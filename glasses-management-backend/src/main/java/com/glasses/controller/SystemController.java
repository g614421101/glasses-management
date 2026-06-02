package com.glasses.controller;

import com.glasses.util.LanIpUtil;
import com.glasses.util.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    @Value("${server.port:8080}")
    private int port;

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
}
