package com.example.provider.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.annotation.RequireLogin;
import com.example.common.annotation.RequirePermission;
import com.example.common.model.Result;
import com.example.provider.entity.SysOperationLog;
import com.example.provider.service.SysOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/api/system/log")
@RequireLogin
public class SysOperationLogController {
    
    @Autowired
    private SysOperationLogService logService;
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 分页查询操作日志
     */
    @GetMapping("/list")
    @RequirePermission("system:log:list")
    public Result<Page<SysOperationLog>> list(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        // 解析日期时间参数，空字符串处理为null
        LocalDateTime startDateTime = parseDateTime(startTime);
        LocalDateTime endDateTime = parseDateTime(endTime);
        
        Page<SysOperationLog> result = logService.pageList(username, module, operationType, status, startDateTime, endDateTime, page, size);
        return Result.success(result);
    }
    
    /**
     * 解析日期时间字符串，空字符串返回null
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取日志详情
     */
    @GetMapping("/{id}")
    @RequirePermission("system:log:query")
    public Result<SysOperationLog> getById(@PathVariable Long id) {
        return Result.success(logService.getById(id));
    }
    
    /**
     * 清理历史日志
     */
    @DeleteMapping("/clean")
    @RequirePermission("system:log:delete")
    public Result<Object> clean(@RequestParam(defaultValue = "30") int days) {
        int count = logService.cleanLogs(days);
        return Result.success("清理成功，共删除 " + count + " 条记录", null);
    }
}
