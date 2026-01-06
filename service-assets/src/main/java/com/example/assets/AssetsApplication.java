package com.example.assets;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.example.assets.mapper")
public class AssetsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AssetsApplication.class, args);
    }
}
