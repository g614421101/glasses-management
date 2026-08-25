package com.glasses.config;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.glasses.util.LanIpUtil;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.net.InetAddress;

@Component
public class MdnsAdvertiser implements ApplicationListener<WebServerInitializedEvent> {

    private static final Logger log = LoggerFactory.getLogger(MdnsAdvertiser.class);
    private static final String SERVICE_TYPE = "_glasses._tcp.local.";

    private final MdnsProperties properties;
    private JmDNS jmdns;

    public MdnsAdvertiser(MdnsProperties properties) {
        this.properties = properties;
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        if (!properties.isEnabled()) {
            log.info("mDNS 广播已禁用");
            return;
        }

        int port = event.getWebServer().getPort();
        String ip = LanIpUtil.findLanIp();
        if (ip == null) {
            log.warn("未检测到局域网 IP，mDNS 广播未启动");
            return;
        }

        try {
            InetAddress addr = InetAddress.getByName(ip);
            jmdns = JmDNS.create(addr, "glasses-management");
            ServiceInfo serviceInfo = ServiceInfo.create(SERVICE_TYPE, properties.getServiceName(), port, "Glasses Management System");
            jmdns.registerService(serviceInfo);
            log.info("mDNS 广播已启动: {} ({}:{})", properties.getServiceName(), ip, port);
        } catch (Exception e) {
            log.error("mDNS 广播启动失败: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (jmdns != null) {
            jmdns.unregisterAllServices();
            try {
                jmdns.close();
            } catch (Exception e) {
                // ignore
            }
            log.info("mDNS 广播已停止");
        }
    }
}
