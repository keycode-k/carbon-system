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
     * @param token 用户token
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestParam String token) {
        return userFeignClient.logout(token);
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
    
    /**
     * 获取用户信息接口
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/info")
    public Map<String, Object> getUserInfo(@RequestParam String username) {
        return userFeignClient.getUserInfo(username);
    }

    /**
     * 更新用户信息接口
     * @param user 用户信息
     * @return 更新结果
     */
    @PostMapping("/update")
    public Map<String, Object> updateUser(@RequestBody User user) {
        return userFeignClient.updateUser(user);
    }

    /**
     * 修改密码接口
     * @param params 包含username, oldPassword, newPassword
     * @return 修改结果
     */
    @PostMapping("/password")
    public Map<String, Object> updatePassword(@RequestBody Map<String, String> params) {
        return userFeignClient.updatePassword(params);
    }

    /**
     * 注销用户接口
     * @param username 用户名
     * @return 注销结果
     */
    @DeleteMapping("/delete")
    public Map<String, Object> deleteUser(@RequestParam String username) {
        return userFeignClient.deleteUser(username);
    }
}
