package com.glasses.controller;

import com.glasses.util.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    @Value("${server.port:8080}")
    private int port;

    @GetMapping("/lan-info")
    public Result<Map<String, Object>> getLanInfo() {
        String ip = findLanIp();
        if (ip == null) {
            return Result.error("未检测到局域网连接");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("ip", ip);
        data.put("port", port);
        return Result.success(data);
    }

    private String findLanIp() {
        try {
            List<InetAddress> candidates = new ArrayList<>();
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp() || ni.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        candidates.add(addr);
                    }
                }
            }
            // 优先返回 192.168.x.x
            for (InetAddress addr : candidates) {
                if (addr.getHostAddress().startsWith("192.168.")) {
                    return addr.getHostAddress();
                }
            }
            // 其次 10.x.x.x
            for (InetAddress addr : candidates) {
                if (addr.getHostAddress().startsWith("10.")) {
                    return addr.getHostAddress();
                }
            }
            // 再次 172.16-31.x.x
            for (InetAddress addr : candidates) {
                String h = addr.getHostAddress();
                if (h.startsWith("172.")) {
                    int second = Integer.parseInt(h.split("\\.")[1]);
                    if (second >= 16 && second <= 31) {
                        return h;
                    }
                }
            }
            // 返回第一个非回环地址
            if (!candidates.isEmpty()) {
                return candidates.get(0).getHostAddress();
            }
        } catch (SocketException e) {
            // ignore
        }
        return null;
    }
}
