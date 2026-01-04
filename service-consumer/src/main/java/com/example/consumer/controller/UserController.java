package com.example.consumer.controller;

import com.example.consumer.entity.User;
import com.example.consumer.feign.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器，提供用户交互接口
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserFeignClient userFeignClient;

    /**
     * 用户注册接口
     * @param user 用户信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        return userFeignClient.register(user);
    }

    /**
     * 用户登录接口
     * @param user 用户信息
     * @return 登录结果
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
        return userFeignClient.login(user);
    }

    /**
     * 用户登出接口
     * @param username 用户名
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestParam String username) {
        return userFeignClient.logout(username);
    }
    
    /**
     * 验证token有效性接口
     * @param token 用户token
     * @return 验证结果
     */
    @GetMapping("/verifyToken")
    public Map<String, Object> verifyToken(@RequestParam String token) {
        return userFeignClient.verifyToken(token);
    }
}
