package com.example.provider.interceptor;

import com.example.common.security.LoginUser;
import com.example.common.security.UserContextHolder;
import com.example.common.util.JwtUtils;
import com.example.provider.service.SysMenuService;
import com.example.provider.service.SysRoleService;
import com.example.provider.entity.SysRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JWT认证拦截器
 * 解析Token，设置用户上下文
 */
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private SysRoleService roleService;
    
    @Autowired
    private SysMenuService menuService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取Authorization头
        String authHeader = request.getHeader(JwtUtils.HEADER_NAME);
        String token = JwtUtils.getTokenFromHeader(authHeader);
        
        if (token != null && jwtUtils.validateToken(token)) {
            try {
                // 解析Token获取用户信息
                Long userId = jwtUtils.getUserId(token);
                String username = jwtUtils.getUsername(token);
                List<String> roles = jwtUtils.getRoles(token);
                
                // 创建登录用户对象
                LoginUser loginUser = new LoginUser(userId, username, roles);
                loginUser.setToken(token);
                
                // 获取用户权限（可选，根据需要加载）
                List<String> permissions = menuService.getPermissionsByUserId(userId);
                loginUser.setPermissions(permissions);
                
                // 设置用户上下文
                UserContextHolder.setUser(loginUser);
                
            } catch (Exception e) {
                // Token解析失败，但不阻断请求，让后续注解处理
                UserContextHolder.clear();
            }
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清理用户上下文，防止内存泄漏
        UserContextHolder.clear();
    }
    
    /**
     * 返回未授权响应
     */
    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("message", message);
        result.put("data", null);
        
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(result));
    }
}
