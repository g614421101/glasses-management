package com.glasses.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BrowserLauncher implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 从环境变量或配置中获取是否自动打开浏览器的开关，默认为 true
        Boolean autoLaunch = event.getApplicationContext().getEnvironment().getProperty("app.browser.auto-launch", Boolean.class, true);
        if (Boolean.FALSE.equals(autoLaunch)) {
            log.info("已跳过自动打开浏览器（根据配置已禁用）");
            return;
        }

        String url = "http://localhost:8080";
        try {
            // 获取当前操作系统名称
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // Windows 系统调用 cmd 的 start 命令打开默认浏览器
                new ProcessBuilder("cmd", "/c", "start", url).start();
                log.info("已自动打开浏览器: {}", url);
            } else if (os.contains("mac")) {
                // Mac 系统使用 open 命令
                new ProcessBuilder("open", url).start();
                log.info("已自动打开浏览器: {}", url);
            } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
                // Linux 系统使用 xdg-open 命令
                new ProcessBuilder("xdg-open", url).start();
                log.info("已自动打开浏览器: {}", url);
            }
        } catch (Exception e) {
            log.error("自动打开浏览器失败，请手动在浏览器访问 {}", url, e);
        }
    }
}
