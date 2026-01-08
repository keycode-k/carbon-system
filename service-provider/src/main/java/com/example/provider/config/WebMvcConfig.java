package com.example.provider.config;

import com.example.provider.interceptor.JwtAuthInterceptor;
import com.example.provider.interceptor.PermissionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 注册拦截器、跨域配置等
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;
    
    @Autowired
    private PermissionInterceptor permissionInterceptor;
    
    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // JWT认证拦截器 - 所有请求都会经过，但不会阻止未认证请求
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 静态资源
                        "/static/**",
                        "/favicon.ico",
                        // Swagger文档
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/v2/api-docs",
                        "/v3/api-docs/**",
                        "/doc.html",
                        // 健康检查
                        "/actuator/**",
                        // 公开接口 - 登录注册
                        "/api/user/login",
                        "/api/user/register",
                        "/api/user/captcha",
                        "/api/user/resetPassword",
                        "/user/login",
                        "/user/register"
                )
                .order(1); // 优先级较高
        
        // 权限校验拦截器 - 检查方法上的权限注解
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/static/**",
                        "/favicon.ico",
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/v2/api-docs",
                        "/v3/api-docs/**",
                        "/doc.html",
                        "/actuator/**",
                        "/api/user/login",
                        "/api/user/register",
                        "/api/user/captcha",
                        "/api/user/resetPassword",
                        "/user/login",
                        "/user/register"
                )
                .order(2); // 在JWT拦截器之后执行
    }
    
    /**
     * 配置跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .exposedHeaders("Authorization")
                .maxAge(3600);
    }
}
