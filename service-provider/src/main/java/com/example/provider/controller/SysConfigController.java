package com.example.provider.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.annotation.OperationLog;
import com.example.common.annotation.OperationLog.OperationType;
import com.example.common.annotation.RequireLogin;
import com.example.common.annotation.RequirePermission;
import com.example.common.model.Result;
import com.example.provider.entity.SysConfig;
import com.example.provider.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统配置管理Controller
 */
@RestController
@RequestMapping("/api/system/config")
@RequireLogin
public class SysConfigController {
    
    @Autowired
    private SysConfigService configService;
    
    /**
     * 分页查询配置列表
     */
    @GetMapping("/list")
    @RequirePermission("system:config:list")
    public Result<Page<SysConfig>> list(
            @RequestParam(required = false) String configName,
            @RequestParam(required = false) String configKey,
            @RequestParam(required = false) String configGroup,
            @RequestParam(required = false) Integer configType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<SysConfig> result = configService.pageList(configName, configKey, configGroup, configType, page, size);
        return Result.success(result);
    }
    
    /**
     * 获取配置详情
     */
    @GetMapping("/{id}")
    @RequirePermission("system:config:query")
    public Result<SysConfig> getById(@PathVariable Long id) {
        SysConfig config = configService.getById(id);
        if (config == null) {
            return Result.error("配置不存在");
        }
        return Result.success(config);
    }
    
    /**
     * 根据配置键名获取配置值
     */
    @GetMapping("/key/{configKey}")
    public Result<String> getByKey(@PathVariable String configKey) {
        String value = configService.getConfigValue(configKey);
        return Result.success(value);
    }
    
    /**
     * 按分组获取配置列表
     */
    @GetMapping("/group/{group}")
    @RequirePermission("system:config:list")
    public Result<List<SysConfig>> listByGroup(@PathVariable String group) {
        List<SysConfig> configs = configService.listByGroup(group);
        return Result.success(configs);
    }
    
    /**
     * 获取所有配置分组
     */
    @GetMapping("/groups")
    @RequirePermission("system:config:list")
    public Result<List<String>> listGroups() {
        List<String> groups = configService.listGroups();
        return Result.success(groups);
    }
    
    /**
     * 新增配置
     */
    @PostMapping
    @RequirePermission("system:config:add")
    @OperationLog(module = "系统配置", type = OperationType.CREATE, description = "新增配置")
    public Result<Object> create(@RequestBody SysConfig config) {
        boolean success = configService.create(config);
        if (success) {
            return Result.success("创建成功", null);
        }
        return Result.error("创建失败，配置键名可能已存在");
    }
    
    /**
     * 更新配置
     */
    @PutMapping
    @RequirePermission("system:config:edit")
    @OperationLog(module = "系统配置", type = OperationType.UPDATE, description = "修改配置")
    public Result<Object> update(@RequestBody SysConfig config) {
        if (config.getId() == null) {
            return Result.error("配置ID不能为空");
        }
        boolean success = configService.update(config);
        if (success) {
            return Result.success("更新成功", null);
        }
        return Result.error("更新失败");
    }
    
    /**
     * 删除配置
     */
    @DeleteMapping("/{id}")
    @RequirePermission("system:config:delete")
    @OperationLog(module = "系统配置", type = OperationType.DELETE, description = "删除配置")
    public Result<Object> delete(@PathVariable Long id) {
        boolean success = configService.delete(id);
        if (success) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败，系统内置配置不允许删除");
    }
    
    /**
     * 批量删除配置
     */
    @DeleteMapping("/batch")
    @RequirePermission("system:config:delete")
    @OperationLog(module = "系统配置", type = OperationType.DELETE, description = "批量删除配置")
    public Result<Object> batchDelete(@RequestBody List<Long> ids) {
        int count = configService.batchDelete(ids);
        return Result.success("成功删除 " + count + " 条配置", null);
    }
    
    /**
     * 刷新缓存
     */
    @PostMapping("/refresh")
    @RequirePermission("system:config:edit")
    @OperationLog(module = "系统配置", type = OperationType.OTHER, description = "刷新配置缓存")
    public Result<Object> refreshCache() {
        configService.refreshCache();
        return Result.success("缓存刷新成功", null);
    }
}
