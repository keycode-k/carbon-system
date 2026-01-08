package com.example.consumer.controller;

import com.example.common.model.Result;
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
     */
    @PostMapping("/register")
    public Result<Object> register(@RequestBody User user) {
        return userFeignClient.register(user);
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User user) {
        return userFeignClient.login(user);
    }
    
    /**
     * 获取用户信息
     * 从请求头获取Authorization，转发给service-provider
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> info(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        return userFeignClient.getUserInfo(authHeader);
    }

    /**
     * 用户登出接口
     */
    @PostMapping("/logout")
    public Result<Object> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        return userFeignClient.logout(authHeader);
    }

    /**
     * 更新用户信息接口
     */
    @PostMapping("/update")
    public Result<User> updateUser(@RequestBody User user) {
        return userFeignClient.updateUser(user);
    }

    /**
     * 修改密码接口
     */
    @PostMapping("/password")
    public Result<Object> updatePassword(@RequestBody Map<String, String> params) {
        return userFeignClient.updatePassword(params);
    }

    /**
     * 注销用户接口
     */
    @DeleteMapping("/delete")
    public Result<Object> deleteUser(@RequestParam Long userId) {
        return userFeignClient.deleteUser(userId);
    }
}
