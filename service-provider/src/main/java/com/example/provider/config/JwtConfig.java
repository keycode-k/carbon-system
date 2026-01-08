package com.example.provider.config;

import com.example.common.util.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JWT配置类
 */
@Configuration
public class JwtConfig {
    
    @Value("${jwt.secret:carbon-system-jwt-secret-key-2026-very-long-key-for-security}")
    private String secret;
    
    @Value("${jwt.expiration:7200000}")
    private long expiration;
    
    @Bean
    public JwtUtils jwtUtils() {
        return new JwtUtils(secret, expiration);
    }
}
