package com.example.provider.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.common.annotation.OperationLog;
import com.example.common.annotation.OperationLog.OperationType;
import com.example.common.model.Result;
import com.example.common.security.LoginUser;
import com.example.common.security.UserContextHolder;
import com.example.common.util.JwtUtils;
import com.example.provider.entity.User;
import com.example.provider.entity.SysRole;
import com.example.provider.mapper.UserMapper;
import com.example.provider.service.SysRoleService;
import com.example.provider.service.SysMenuService;
import com.example.provider.util.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    
    // 注入JwtUtils，用于生成JWT Token
    @Autowired
    private JwtUtils jwtUtils;
    
    // 注入角色服务
    @Autowired
    private SysRoleService sysRoleService;
    
    // 注入菜单服务（获取权限）
    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 用户注册接口
     * @param user 用户信息
     * @return 注册结果
     */
    @PostMapping("/register")
    @OperationLog(module = "用户管理", type = OperationType.CREATE, description = "用户注册", saveParams = false)
    public Result<Object> register(@RequestBody User user) {
        // 查询用户名是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        User existUser = userMapper.selectOne(queryWrapper);
        
        if (existUser != null) {
            return Result.error("用户名已存在");
        } else {
            // 密码BCrypt加密
            user.setPassword(PasswordUtils.encode(user.getPassword()));
            // 设置默认状态
            if (user.getStatus() == null) {
                user.setStatus(1);
            }
            user.setCreateTime(java.time.LocalDateTime.now());
            // 插入新用户
            userMapper.insert(user);
            return Result.success("注册成功", null);
        }
    }
    
    /**
     * 用户登录接口
     * @param user 用户信息
     * @return 登录结果（含JWT Token）
     */
    @PostMapping("/login")
    @OperationLog(module = "用户管理", type = OperationType.LOGIN, description = "用户登录", saveParams = false, saveResult = false)
    public Result<Map<String, Object>> login(@RequestBody User user) {
        // 查询用户是否存在（只按用户名查询）
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        User existUser = userMapper.selectOne(queryWrapper);
        
        // 验证用户存在且密码正确
        if (existUser != null && PasswordUtils.matches(user.getPassword(), existUser.getPassword())) {
            // 检查用户状态
            if (existUser.getStatus() != null && existUser.getStatus() == 0) {
                return Result.error("账户已被禁用，请联系管理员");
            }
            // 获取用户角色
            List<SysRole> roles = sysRoleService.getUserRoles(existUser.getId());
            List<String> roleKeys = roles.stream()
                    .map(SysRole::getRoleCode)
                    .collect(Collectors.toList());
            
            // 获取用户权限
            Set<String> permissions = sysMenuService.getUserPermissions(existUser.getId());
            
            // 生成JWT Token
            String token = jwtUtils.generateToken(existUser.getId(), existUser.getUsername(), roleKeys);
            String refreshToken = jwtUtils.generateRefreshToken(existUser.getId(), existUser.getUsername());
            
            // 将用户信息存入Redis，用于快速验证（可选）
            redisTemplate.opsForValue().set("user:" + existUser.getId(), token, 24, TimeUnit.HOURS);
            
            // 返回token和用户基本信息
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("refreshToken", refreshToken);
            data.put("userId", existUser.getId());
            data.put("username", existUser.getUsername());
            data.put("nickname", existUser.getNickname());
            data.put("roles", roleKeys);
            data.put("permissions", permissions);
            
            return Result.success("登录成功", data);
        } else {
            return Result.error("用户名或密码错误");
        }
    }
    
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 优先从UserContextHolder获取当前登录用户
        LoginUser loginUser = UserContextHolder.getUser();
        if (loginUser != null) {
            User user = userMapper.selectById(loginUser.getUserId());
            if (user != null) {
                user.setPassword(null);
                
                Map<String, Object> data = new HashMap<>();
                data.put("userId", user.getId());
                data.put("username", user.getUsername());
                data.put("nickname", user.getNickname());
                data.put("email", user.getEmail());
                data.put("phone", user.getPhone());
                data.put("roles", loginUser.getRoles());
                data.put("permissions", loginUser.getPermissions());
                
                return Result.success(data);
            }
        }
        
        // 兼容旧的token方式
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtils.validateToken(token)) {
                Long userId = jwtUtils.getUserId(token);
                User user = userMapper.selectById(userId);
                if (user != null) {
                    user.setPassword(null);
                    
                    // 获取用户角色和权限
                    List<SysRole> roles = sysRoleService.getUserRoles(userId);
                    List<String> roleKeys = roles.stream()
                            .map(SysRole::getRoleCode)
                            .collect(Collectors.toList());
                    Set<String> permissions = sysMenuService.getUserPermissions(userId);
                    
                    Map<String, Object> data = new HashMap<>();
                    data.put("userId", user.getId());
                    data.put("username", user.getUsername());
                    data.put("nickname", user.getNickname());
                    data.put("email", user.getEmail());
                    data.put("phone", user.getPhone());
                    data.put("roles", roleKeys);
                    data.put("permissions", permissions);
                    
                    return Result.success(data);
                }
            }
        }
        
        return Result.error(401, "Token无效或已过期");
    }

    /**
     * 用户登出接口
     * @return 登出结果
     */
    @PostMapping("/logout")
    @OperationLog(module = "用户管理", type = OperationType.LOGOUT, description = "用户登出")
    public Result<Object> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 从UserContextHolder获取当前用户
        LoginUser loginUser = UserContextHolder.getUser();
        if (loginUser != null) {
            // 从Redis中删除用户token
            redisTemplate.delete("user:" + loginUser.getUserId());
        }
        
        // 兼容旧的token方式
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtils.validateToken(token)) {
                Long userId = jwtUtils.getUserId(token);
                redisTemplate.delete("user:" + userId);
            }
        }
        
        // 清除当前线程的用户上下文
        UserContextHolder.clear();
        
        return Result.success("登出成功", null);
    }
    
    /**
     * 更新用户信息接口
     */
    @PostMapping("/update")
    @OperationLog(module = "用户管理", type = OperationType.UPDATE, description = "更新个人信息")
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
    @OperationLog(module = "用户管理", type = OperationType.UPDATE, description = "修改密码", saveParams = false)
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
        
        // 使用BCrypt验证旧密码
        if (!PasswordUtils.matches(oldPassword, user.getPassword())) {
            return Result.error("旧密码错误");
        }
        
        // 新密码加密存储
        user.setPassword(PasswordUtils.encode(newPassword));
        userMapper.updateById(user);
        
        return Result.success("密码修改成功", null);
    }
    
    /**
     * 注销用户
     */
    @DeleteMapping("/delete")
    @OperationLog(module = "用户管理", type = OperationType.DELETE, description = "注销账户")
    public Result<Object> deleteUser(@RequestParam("userId") Long userId) {
        // 同样应该做权限校验
        userMapper.deleteById(userId);
        return Result.success("账户已注销", null);
    }
}
