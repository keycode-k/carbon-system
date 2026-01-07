package com.example.development;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@org.mybatis.spring.annotation.MapperScan("com.example.development.mapper")
public class DevelopmentApplication {
    public static void main(String[] args) {
        SpringApplication.run(DevelopmentApplication.class, args);
    }
}
