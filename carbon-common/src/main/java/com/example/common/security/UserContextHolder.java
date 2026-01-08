package com.example.common.security;

/**
 * 用户上下文工具类
 * 使用ThreadLocal存储当前登录用户信息
 */
public class UserContextHolder {
    
    private static final ThreadLocal<LoginUser> USER_CONTEXT = new ThreadLocal<>();
    
    /**
     * 设置当前用户
     */
    public static void setUser(LoginUser user) {
        USER_CONTEXT.set(user);
    }
    
    /**
     * 获取当前用户
     */
    public static LoginUser getUser() {
        return USER_CONTEXT.get();
    }
    
    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        LoginUser user = getUser();
        return user != null ? user.getUserId() : null;
    }
    
    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        LoginUser user = getUser();
        return user != null ? user.getUsername() : null;
    }
    
    /**
     * 判断当前用户是否有某个角色
     */
    public static boolean hasRole(String role) {
        LoginUser user = getUser();
        return user != null && user.hasRole(role);
    }
    
    /**
     * 判断当前用户是否有某个权限
     */
    public static boolean hasPermission(String permission) {
        LoginUser user = getUser();
        return user != null && user.hasPermission(permission);
    }
    
    /**
     * 判断当前用户是否为管理员
     */
    public static boolean isAdmin() {
        LoginUser user = getUser();
        return user != null && user.isAdmin();
    }
    
    /**
     * 清除当前用户（请求结束时调用）
     */
    public static void clear() {
        USER_CONTEXT.remove();
    }
}
