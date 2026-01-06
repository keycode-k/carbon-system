package com.example.provider.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.provider.entity.User;
import com.example.provider.mapper.UserMapper;
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

    // 注入UserMapper，用于操作数据库
    @Autowired
    private UserMapper userMapper;
    
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
        
        // 查询用户名是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        User existUser = userMapper.selectOne(queryWrapper);
        
        if (existUser != null) {
            result.put("success", false);
            result.put("message", "用户名已存在");
        } else {
            // 插入新用户
            userMapper.insert(user);
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
        
        try {
            System.out.println("接收到登录请求 - 用户名: " + user.getUsername());
            
            // 查询用户
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", user.getUsername());
            queryWrapper.eq("password", user.getPassword());
            User dbUser = userMapper.selectOne(queryWrapper);
            
            System.out.println("数据库查询结果: " + (dbUser != null ? "找到用户" : "未找到用户"));
            
            if (dbUser != null) {
                // 生成随机token，长度为32位
                String token = RandomStringUtils.randomAlphanumeric(32);
                System.out.println("生成token: " + token);
                
                // 将token存储到Redis，key为"user:token:"+token，value为用户名，过期时间为24小时
                redisTemplate.opsForValue().set("user:token:" + token, user.getUsername(), 24, TimeUnit.HOURS);
                System.out.println("已将token存储到Redis");
                
                result.put("success", true);
                result.put("message", "登录成功");
                result.put("token", token);
            } else {
                result.put("success", false);
                result.put("message", "用户名或密码错误");
            }
        } catch (Exception e) {
            System.err.println("登录异常: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "登录失败: " + e.getMessage());
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
