package com.example.provider.controller;

import com.example.provider.entity.User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器，提供注册、登录和登出接口
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    // 模拟用户数据库
    private static final Map<String, String> userMap = new HashMap<>();

    /**
     * 用户注册接口
     * @param user 用户信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        
        if (userMap.containsKey(user.getUsername())) {
            result.put("success", false);
            result.put("message", "用户名已存在");
        } else {
            userMap.put(user.getUsername(), user.getPassword());
            result.put("success", true);
            result.put("message", "注册成功");
        }
        
        return result;
    }

    /**
     * 用户登录接口
     * @param user 用户信息
     * @return 登录结果
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        
        if (userMap.containsKey(user.getUsername()) && userMap.get(user.getUsername()).equals(user.getPassword())) {
            result.put("success", true);
            result.put("message", "登录成功");
        } else {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
        }
        
        return result;
    }

    /**
     * 用户登出接口
     * @param username 用户名
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestParam String username) {
        Map<String, Object> result = new HashMap<>();
        
        // 模拟登出逻辑，实际项目中可能需要处理token等
        result.put("success", true);
        result.put("message", "登出成功");
        
        return result;
    }
}
