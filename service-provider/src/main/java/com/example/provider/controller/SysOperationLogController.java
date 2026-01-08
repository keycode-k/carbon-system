package com.example.provider.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.annotation.RequireLogin;
import com.example.common.annotation.RequirePermission;
import com.example.common.model.Result;
import com.example.provider.entity.SysOperationLog;
import com.example.provider.service.SysOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/api/system/log")
@RequireLogin
public class SysOperationLogController {
    
    @Autowired
    private SysOperationLogService logService;
    
    /**
     * 分页查询操作日志
     */
    @GetMapping("/list")
    @RequirePermission("system:log:list")
    public Result<Page<SysOperationLog>> list(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<SysOperationLog> result = logService.pageList(username, module, operationType, startTime, endTime, page, size);
        return Result.success(result);
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
