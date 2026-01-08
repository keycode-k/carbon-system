package com.example.provider.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.provider.entity.SysOperationLog;
import com.example.provider.mapper.SysOperationLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作日志Service
 */
@Service
public class SysOperationLogService extends ServiceImpl<SysOperationLogMapper, SysOperationLog> {
    
    @Autowired
    private SysOperationLogMapper logMapper;
    
    /**
     * 异步保存操作日志
     */
    @Async
    public void saveLogAsync(SysOperationLog log) {
        if (log.getCreateTime() == null) {
            log.setCreateTime(LocalDateTime.now());
        }
        logMapper.insert(log);
    }
    
    /**
     * 同步保存操作日志
     */
    public void saveLog(SysOperationLog log) {
        if (log.getCreateTime() == null) {
            log.setCreateTime(LocalDateTime.now());
        }
        logMapper.insert(log);
    }
    
    /**
     * 分页查询操作日志
     */
    public Page<SysOperationLog> pageList(String username, String module, String operationType,
                                          LocalDateTime startTime, LocalDateTime endTime,
                                          int page, int size) {
        LambdaQueryWrapper<SysOperationLog> query = new LambdaQueryWrapper<>();
        
        if (username != null && !username.isEmpty()) {
            query.like(SysOperationLog::getUsername, username);
        }
        if (module != null && !module.isEmpty()) {
            query.eq(SysOperationLog::getModule, module);
        }
        if (operationType != null && !operationType.isEmpty()) {
            query.eq(SysOperationLog::getOperationType, operationType);
        }
        if (startTime != null) {
            query.ge(SysOperationLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            query.le(SysOperationLog::getCreateTime, endTime);
        }
        
        query.orderByDesc(SysOperationLog::getCreateTime);
        
        return logMapper.selectPage(new Page<>(page, size), query);
    }
    
    /**
     * 清理指定天数之前的日志
     */
    public int cleanLogs(int days) {
        LocalDateTime deadline = LocalDateTime.now().minusDays(days);
        LambdaQueryWrapper<SysOperationLog> query = new LambdaQueryWrapper<>();
        query.lt(SysOperationLog::getCreateTime, deadline);
        return logMapper.delete(query);
    }
}
