package com.example.provider.aspect;

import com.example.common.annotation.OperationLog;
import com.example.common.security.LoginUser;
import com.example.common.security.UserContextHolder;
import com.example.provider.entity.SysOperationLog;
import com.example.provider.service.SysOperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 * 拦截标注了 @OperationLog 注解的方法，自动记录操作日志
 */
@Aspect
@Component
public class OperationLogAspect {
    
    @Autowired
    private SysOperationLogService logService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 切点：所有标注了 @OperationLog 注解的方法
     */
    @Pointcut("@annotation(com.example.common.annotation.OperationLog)")
    public void logPointCut() {}
    
    /**
     * 环绕通知
     */
    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog annotation = method.getAnnotation(OperationLog.class);
        
        // 创建日志对象
        SysOperationLog log = new SysOperationLog();
        log.setModule(annotation.module());
        log.setOperationType(annotation.type().name());
        log.setDescription(annotation.description());
        log.setMethod(joinPoint.getTarget().getClass().getName() + "." + method.getName());
        log.setCreateTime(LocalDateTime.now());
        
        // 获取请求信息
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                log.setRequestUrl(request.getRequestURI());
                log.setIp(getIpAddress(request));
                log.setBrowser(getBrowser(request));
                log.setOs(getOs(request));
            }
        } catch (Exception e) {
            // 忽略获取请求信息的异常
        }
        
        // 获取当前用户
        LoginUser loginUser = UserContextHolder.getUser();
        if (loginUser != null) {
            log.setUserId(loginUser.getUserId());
            log.setUsername(loginUser.getUsername());
        }
        
        // 保存请求参数
        if (annotation.saveParams()) {
            try {
                Object[] args = joinPoint.getArgs();
                if (args != null && args.length > 0) {
                    String params = objectMapper.writeValueAsString(args);
                    // 限制参数长度
                    if (params.length() > 2000) {
                        params = params.substring(0, 2000) + "...";
                    }
                    log.setRequestParams(params);
                }
            } catch (Exception e) {
                log.setRequestParams("参数序列化失败");
            }
        }
        
        Object result = null;
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            log.setStatus(1); // 成功
            
            // 保存响应结果
            if (annotation.saveResult() && result != null) {
                try {
                    String resultStr = objectMapper.writeValueAsString(result);
                    if (resultStr.length() > 2000) {
                        resultStr = resultStr.substring(0, 2000) + "...";
                    }
                    log.setResponseResult(resultStr);
                } catch (Exception e) {
                    log.setResponseResult("结果序列化失败");
                }
            }
        } catch (Throwable e) {
            log.setStatus(0); // 失败
            log.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            // 计算执行时长
            log.setDuration(System.currentTimeMillis() - startTime);
            // 异步保存日志
            logService.saveLogAsync(log);
        }
        
        return result;
    }
    
    /**
     * 获取IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时，第一个IP为真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * 获取浏览器信息
     */
    private String getBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) return "Unknown";
        
        if (userAgent.contains("Chrome")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari")) return "Safari";
        if (userAgent.contains("Edge")) return "Edge";
        if (userAgent.contains("MSIE") || userAgent.contains("Trident")) return "IE";
        return "Unknown";
    }
    
    /**
     * 获取操作系统信息
     */
    private String getOs(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) return "Unknown";
        
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Mac")) return "macOS";
        if (userAgent.contains("Linux")) return "Linux";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) return "iOS";
        return "Unknown";
    }
}
