package com.example.development.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.model.Result;
import com.example.development.entity.Methodology;
import com.example.development.service.MethodologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 方法学管理控制器
 */
@RestController
@RequestMapping("/api/development/methodology")
public class MethodologyController {

    @Autowired
    private MethodologyService methodologyService;

    /**
     * 分页查询方法学列表
     */
    @GetMapping("/list")
    public Result<Page<Methodology>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        
        Page<Methodology> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Methodology> queryWrapper = new LambdaQueryWrapper<>();
        
        // 名称模糊查询
        if (name != null && !name.isEmpty()) {
            queryWrapper.like(Methodology::getName, name);
        }
        
        // 分类筛选
        if (category != null && !category.isEmpty()) {
            queryWrapper.eq(Methodology::getCategory, category);
        }
        
        // 状态筛选
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(Methodology::getStatus, status);
        }
        
        // 按发布时间倒序
        queryWrapper.orderByDesc(Methodology::getPublishDate);
        
        return Result.success(methodologyService.page(pageParam, queryWrapper));
    }

    /**
     * 查询所有有效方法学（不分页，用于下拉选择）
     */
    @GetMapping("/active")
    public Result<List<Methodology>> listActive() {
        LambdaQueryWrapper<Methodology> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Methodology::getStatus, "ACTIVE");
        queryWrapper.orderByDesc(Methodology::getPublishDate);
        return Result.success(methodologyService.list(queryWrapper));
    }

    /**
     * 根据ID查询方法学详情
     */
    @GetMapping("/{id}")
    public Result<Methodology> getById(@PathVariable Long id) {
        Methodology methodology = methodologyService.getById(id);
        if (methodology != null) {
            return Result.success(methodology);
        }
        return Result.error("方法学不存在");
    }

    /**
     * 新增方法学
     */
    @PostMapping
    public Result<Boolean> create(@RequestBody Methodology methodology) {
        methodology.setCreateTime(LocalDateTime.now());
        methodology.setUpdateTime(LocalDateTime.now());
        if (methodology.getStatus() == null) {
            methodology.setStatus("ACTIVE");
        }
        return Result.success(methodologyService.save(methodology));
    }

    /**
     * 更新方法学
     */
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody Methodology methodology) {
        methodology.setId(id);
        methodology.setUpdateTime(LocalDateTime.now());
        return Result.success(methodologyService.updateById(methodology));
    }

    /**
     * 删除方法学
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(methodologyService.removeById(id));
    }

    /**
     * 批量删除方法学
     */
    @DeleteMapping("/batch")
    public Result<Boolean> deleteBatch(@RequestBody List<Long> ids) {
        return Result.success(methodologyService.removeByIds(ids));
    }
}
