package com.glasses;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.glasses.mapper")
public class GlassesApplication {
    public static void main(String[] args) {
        SpringApplication.run(GlassesApplication.class, args);
    }
}
