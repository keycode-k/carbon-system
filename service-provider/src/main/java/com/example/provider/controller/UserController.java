package com.example.provider.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.provider.common.Result;
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
    public Result<Object> register(@RequestBody User user) {
        // 查询用户名是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        User existUser = userMapper.selectOne(queryWrapper);
        
        if (existUser != null) {
            return Result.error("用户名已存在");
        } else {
            // 插入新用户
            userMapper.insert(user);
            return Result.success("注册成功", null);
        }
    }
    
    /**
     * 用户登录接口
     * @param user 用户信息
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User user) {
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        queryWrapper.eq("password", user.getPassword());
        User existUser = userMapper.selectOne(queryWrapper);
        
        if (existUser != null) {
            // 生成token
            String token = RandomStringUtils.randomAlphanumeric(32);
            // 将token存入Redis，设置过期时间为24小时
            redisTemplate.opsForValue().set(token, String.valueOf(existUser.getId()), 24, TimeUnit.HOURS);
            
            // 返回token
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            return Result.success("登录成功", data);
        } else {
            return Result.error("用户名或密码错误");
        }
    }
    
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestParam("token") String token) {
        String userIdStr = redisTemplate.opsForValue().get(token);
        if (userIdStr == null) {
            return Result.error(401, "Token无效或已过期");
        }
        Long userId = Long.valueOf(userIdStr);
        User user = userMapper.selectById(userId);
        if (user != null) {
             user.setPassword(null);
             return Result.success(user);
        }
        return Result.error("用户不存在");
    }

    /**
     * 用户登出接口
     * @param token 用户token
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result<Object> logout(@RequestParam("token") String token) {
        redisTemplate.delete(token);
        return Result.success("登出成功", null);
    }
    
    /**
     * 更新用户信息接口
     */
    @PostMapping("/update")
    public Result<User> updateUser(@RequestBody User user) {
        // 简单实现，实际应校验权限，这里假设前端传来的ID是合法的
        // 更好的做法是从Token获取ID，校验是否修改自己的信息
        if (user.getId() == null) {
             return Result.error("用户ID不能为空");
        }
        User existingUser = userMapper.selectById(user.getId());
        if(existingUser == null) {
             return Result.error("用户不存在");
        }
        
        // 允许修改的字段
        if(user.getNickname() != null) existingUser.setNickname(user.getNickname());
        if(user.getEmail() != null) existingUser.setEmail(user.getEmail());
        if(user.getPhone() != null) existingUser.setPhone(user.getPhone());
        
        userMapper.updateById(existingUser);
        existingUser.setPassword(null);
        return Result.success("更新成功", existingUser);
    }

    /**
     * 修改密码接口
     * @param params 包含 userId, oldPassword, newPassword
     */
    @PostMapping("/password")
    public Result<Object> updatePassword(@RequestBody Map<String, String> params) {
        String userIdStr = params.get("userId");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        
        if (userIdStr == null || oldPassword == null || newPassword == null) {
            return Result.error("参数不完整");
        }
        
        Long userId = Long.valueOf(userIdStr);
        User user = userMapper.selectById(userId);
        
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        if (!user.getPassword().equals(oldPassword)) {
            return Result.error("旧密码错误");
        }
        
        user.setPassword(newPassword);
        userMapper.updateById(user);
        
        return Result.success("密码修改成功", null);
    }
    
    /**
     * 注销用户
     */
    @DeleteMapping("/delete")
    public Result<Object> deleteUser(@RequestParam("userId") Long userId) {
        // 同样应该做权限校验
        userMapper.deleteById(userId);
        return Result.success("账户已注销", null);
    }
}
