package com.example.provider.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.annotation.OperationLog;
import com.example.common.annotation.OperationLog.OperationType;
import com.example.common.annotation.RequireLogin;
import com.example.common.annotation.RequirePermission;
import com.example.common.model.Result;
import com.example.provider.entity.User;
import com.example.provider.entity.SysRole;
import com.example.provider.mapper.UserMapper;
import com.example.provider.service.SysRoleService;
import com.example.provider.util.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统用户管理控制器（管理员专用）
 * 提供用户的增删改查、角色分配等功能
 */
@RestController
@RequestMapping("/api/system/user")
@RequireLogin
public class SysUserController {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private SysRoleService sysRoleService;
    
    /**
     * 分页查询用户列表
     */
    @GetMapping("/list")
    @RequirePermission("system:user:list")
    public Result<Page<Map<String, Object>>> list(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        if (username != null && !username.isEmpty()) {
            queryWrapper.like(User::getUsername, username);
        }
        if (phone != null && !phone.isEmpty()) {
            queryWrapper.like(User::getPhone, phone);
        }
        if (status != null) {
            queryWrapper.eq(User::getStatus, status);
        }
        queryWrapper.orderByDesc(User::getId);
        
        Page<User> pageResult = userMapper.selectPage(new Page<>(page, size), queryWrapper);
        
        // 转换为包含角色信息的Map
        List<Map<String, Object>> records = pageResult.getRecords().stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("nickname", user.getNickname());
            userMap.put("email", user.getEmail());
            userMap.put("phone", user.getPhone());
            userMap.put("status", user.getStatus());
            userMap.put("createTime", user.getCreateTime());
            
            // 获取用户角色
            List<SysRole> roles = sysRoleService.getUserRoles(user.getId());
            userMap.put("roles", roles);
            
            return userMap;
        }).collect(Collectors.toList());
        
        Page<Map<String, Object>> resultPage = new Page<>(page, size, pageResult.getTotal());
        resultPage.setRecords(records);
        
        return Result.success(resultPage);
    }
    
    /**
     * 创建用户
     */
    @PostMapping
    @RequirePermission("system:user:add")
    @OperationLog(module = "系统用户管理", type = OperationType.CREATE, description = "创建用户", saveParams = false)
    public Result<Object> create(@RequestBody User user) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, user.getUsername());
        if (userMapper.selectCount(queryWrapper) > 0) {
            return Result.error("用户名已存在");
        }
        
        // 设置默认值
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        // 密码BCrypt加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(PasswordUtils.encode(user.getPassword()));
        } else {
            // 默认密码
            user.setPassword(PasswordUtils.encode("123456"));
        }
        user.setCreateTime(LocalDateTime.now());
        
        userMapper.insert(user);
        return Result.success("创建成功", null);
    }
    
    /**
     * 更新用户
     */
    @PutMapping
    @RequirePermission("system:user:edit")
    @OperationLog(module = "系统用户管理", type = OperationType.UPDATE, description = "更新用户")
    public Result<Object> update(@RequestBody User user) {
        if (user.getId() == null) {
            return Result.error("用户ID不能为空");
        }
        
        User existingUser = userMapper.selectById(user.getId());
        if (existingUser == null) {
            return Result.error("用户不存在");
        }
        
        // 更新字段
        if (user.getNickname() != null) existingUser.setNickname(user.getNickname());
        if (user.getEmail() != null) existingUser.setEmail(user.getEmail());
        if (user.getPhone() != null) existingUser.setPhone(user.getPhone());
        if (user.getStatus() != null) existingUser.setStatus(user.getStatus());
        
        userMapper.updateById(existingUser);
        return Result.success("更新成功", null);
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    @RequirePermission("system:user:delete")
    @OperationLog(module = "系统用户管理", type = OperationType.DELETE, description = "删除用户")
    public Result<Object> delete(@PathVariable Long userId) {
        userMapper.deleteById(userId);
        return Result.success("删除成功", null);
    }
    
    /**
     * 重置用户密码
     */
    @PostMapping("/resetPassword")
    @RequirePermission("system:user:resetPwd")
    @OperationLog(module = "系统用户管理", type = OperationType.UPDATE, description = "重置用户密码", saveParams = false)
    public Result<Object> resetPassword(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String newPassword = (String) params.get("newPassword");
        
        if (userId == null || newPassword == null) {
            return Result.error("参数不完整");
        }
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 密码BCrypt加密
        user.setPassword(PasswordUtils.encode(newPassword));
        userMapper.updateById(user);
        
        return Result.success("密码重置成功", null);
    }
    
    /**
     * 获取用户角色ID列表
     */
    @GetMapping("/{userId}/roleIds")
    @RequirePermission("system:user:query")
    public Result<List<Long>> getUserRoleIds(@PathVariable Long userId) {
        List<SysRole> roles = sysRoleService.getUserRoles(userId);
        List<Long> roleIds = roles.stream()
                .map(SysRole::getId)
                .collect(Collectors.toList());
        return Result.success(roleIds);
    }
    
    /**
     * 为用户分配角色
     */
    @PostMapping("/roles")
    @RequirePermission("system:user:edit")
    @OperationLog(module = "系统用户管理", type = OperationType.UPDATE, description = "分配用户角色")
    public Result<Object> assignRoles(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        @SuppressWarnings("unchecked")
        List<Integer> roleIdInts = (List<Integer>) params.get("roleIds");
        List<Long> roleIds = roleIdInts.stream()
                .map(Integer::longValue)
                .collect(Collectors.toList());
        
        sysRoleService.assignRolesToUser(userId, roleIds);
        return Result.success("角色分配成功", null);
    }
}
