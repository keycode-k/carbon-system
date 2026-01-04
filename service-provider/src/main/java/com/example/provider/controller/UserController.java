package com.example.provider.controller;

import com.example.provider.entity.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户控制器，提供注册、登录和登出接口
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    // 模拟用户数据库
    private static final Map<String, String> userMap = new HashMap<>();
    
    // 注入RedisTemplate，用于操作Redis
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

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
     * @return 登录结果，包含token
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        
        if (userMap.containsKey(user.getUsername()) && userMap.get(user.getUsername()).equals(user.getPassword())) {
            // 生成随机token，长度为32位
            String token = RandomStringUtils.randomAlphanumeric(32);
            
            // 将token存储到Redis，key为"user:token:"+token，value为用户名，过期时间为24小时
            redisTemplate.opsForValue().set("user:token:" + token, user.getUsername(), 24, TimeUnit.HOURS);
            
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("token", token);
        } else {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
        }
        
        return result;
    }

    /**
     * 用户登出接口
     * @param token 用户token
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestParam String token) {
        Map<String, Object> result = new HashMap<>();
        
        // 从Redis删除token
        redisTemplate.delete("user:token:" + token);
        
        result.put("success", true);
        result.put("message", "登出成功");
        
        return result;
    }
    
    /**
     * 验证token有效性
     * @param token 用户token
     * @return token验证结果
     */
    @GetMapping("/verifyToken")
    public Map<String, Object> verifyToken(@RequestParam String token) {
        Map<String, Object> result = new HashMap<>();
        
        // 从Redis获取token对应的用户名
        String username = redisTemplate.opsForValue().get("user:token:" + token);
        
        if (username != null) {
            result.put("success", true);
            result.put("message", "token有效");
            result.put("username", username);
        } else {
            result.put("success", false);
            result.put("message", "token无效或已过期");
        }
        
        return result;
    }
}
