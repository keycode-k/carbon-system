package com.example.news;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 碳资讯服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.example.news.mapper")
public class NewsApplication {
    public static void main(String[] args) {
        SpringApplication.run(NewsApplication.class, args);
        System.out.println("========================================");
        System.out.println("碳资讯服务启动成功！端口: 8086");
        System.out.println("========================================");
    }
}
