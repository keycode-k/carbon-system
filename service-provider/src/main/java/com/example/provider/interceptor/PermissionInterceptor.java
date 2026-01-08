package com.example.provider.interceptor;

import com.example.common.annotation.RequireLogin;
import com.example.common.annotation.RequirePermission;
import com.example.common.annotation.RequireRole;
import com.example.common.security.LoginUser;
import com.example.common.security.UserContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 权限校验拦截器
 * 检查用户是否拥有访问接口所需的权限
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理方法调用
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        Class<?> clazz = handlerMethod.getBeanType();
        
        // 检查是否需要登录
        RequireLogin requireLogin = method.getAnnotation(RequireLogin.class);
        if (requireLogin == null) {
            requireLogin = clazz.getAnnotation(RequireLogin.class);
        }
        
        if (requireLogin != null && requireLogin.required()) {
            LoginUser user = UserContextHolder.getUser();
            if (user == null) {
                writeResponse(response, 401, "请先登录");
                return false;
            }
        }
        
        // 检查角色
        RequireRole requireRole = method.getAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = clazz.getAnnotation(RequireRole.class);
        }
        
        if (requireRole != null) {
            LoginUser user = UserContextHolder.getUser();
            if (user == null) {
                writeResponse(response, 401, "请先登录");
                return false;
            }
            
            String[] roles = requireRole.value();
            RequirePermission.Logical logical = requireRole.logical();
            
            boolean hasRole = checkRoles(user, roles, logical);
            if (!hasRole) {
                writeResponse(response, 403, "权限不足，需要角色: " + String.join(" / ", roles));
                return false;
            }
        }
        
        // 检查权限
        RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);
        if (requirePermission == null) {
            requirePermission = clazz.getAnnotation(RequirePermission.class);
        }
        
        if (requirePermission != null) {
            LoginUser user = UserContextHolder.getUser();
            if (user == null) {
                writeResponse(response, 401, "请先登录");
                return false;
            }
            
            String permission = requirePermission.value();
            if (!user.hasPermission(permission)) {
                writeResponse(response, 403, "权限不足，需要权限: " + permission);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 检查角色
     */
    private boolean checkRoles(LoginUser user, String[] roles, RequirePermission.Logical logical) {
        if (logical == RequirePermission.Logical.AND) {
            // 需要拥有所有角色
            for (String role : roles) {
                if (!user.hasRole(role)) {
                    return false;
                }
            }
            return true;
        } else {
            // 只需要拥有其中一个角色
            for (String role : roles) {
                if (user.hasRole(role)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    /**
     * 写入响应
     */
    private void writeResponse(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code == 401 ? HttpServletResponse.SC_UNAUTHORIZED : HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        result.put("data", null);
        
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(result));
    }
}
