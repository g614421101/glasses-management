package com.glasses.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.File;

public class SqliteDirectoryInitializer implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String url = environment.getProperty("spring.datasource.url");
        if (url == null) {
            url = "jdbc:sqlite:" + System.getProperty("user.home") + "/.glasses_management/data/glasses_management.db";
        }
        if (url.startsWith("jdbc:sqlite:")) {
            String path = url.substring("jdbc:sqlite:".length());
            int queryIdx = path.indexOf('?');
            if (queryIdx != -1) {
                path = path.substring(0, queryIdx);
            }
            if (!":memory:".equals(path) && !path.startsWith("file::memory:") && !path.trim().isEmpty()) {
                try {
                    File dbFile = new File(path);
                    File parentDir = dbFile.getParentFile();
                    if (parentDir != null && !parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }
}
