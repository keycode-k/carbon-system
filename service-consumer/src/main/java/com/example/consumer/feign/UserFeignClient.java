package com.example.consumer.feign;

import com.example.consumer.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "service-provider")
public interface UserFeignClient {

    /**
     * 调用服务提供模块的注册接口
     * @param user 用户信息
     * @return 注册结果
     */
    @PostMapping("/api/user/register")
    Map<String, Object> register(@RequestBody User user);

    /**
     * 调用服务提供模块的登录接口
     * @param user 用户信息
     * @return 登录结果
     */
    @PostMapping("/api/user/login")
    Map<String, Object> login(@RequestBody User user);

    /**
     * 调用服务提供模块的登出接口
     * @param username 用户名
     * @return 登出结果
     */
    @PostMapping("/api/user/logout")
    Map<String, Object> logout(@RequestParam("username") String username);
    
    /**
     * 调用服务提供模块的验证token接口
     * @param token 用户token
     * @return 验证结果
     */
    @GetMapping("/api/user/verifyToken")
    Map<String, Object> verifyToken(@RequestParam("token") String token);
}
