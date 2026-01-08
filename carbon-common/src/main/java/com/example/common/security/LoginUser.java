package com.example.common.security;

import java.util.List;

/**
 * 登录用户信息
 * 存储在ThreadLocal中，方便在请求处理过程中获取当前用户信息
 */
public class LoginUser {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 角色列表
     */
    private List<String> roles;
    
    /**
     * 权限列表
     */
    private List<String> permissions;
    
    /**
     * Token
     */
    private String token;
    
    public LoginUser() {
    }
    
    public LoginUser(Long userId, String username, List<String> roles) {
        this.userId = userId;
        this.username = username;
        this.roles = roles;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public List<String> getRoles() {
        return roles;
    }
    
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    
    public List<String> getPermissions() {
        return permissions;
    }
    
    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    /**
     * 是否拥有某个角色
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
    
    /**
     * 是否拥有某个权限
     */
    public boolean hasPermission(String permission) {
        // 超级管理员拥有所有权限
        if (hasRole("SUPER_ADMIN")) {
            return true;
        }
        return permissions != null && permissions.contains(permission);
    }
    
    /**
     * 是否为管理员
     */
    public boolean isAdmin() {
        return hasRole("SUPER_ADMIN") || hasRole("ADMIN");
    }
}
