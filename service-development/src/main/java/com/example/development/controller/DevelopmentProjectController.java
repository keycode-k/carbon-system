package com.example.development.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.model.Result;
import com.example.development.entity.DevelopmentProject;
import com.example.development.service.DevelopmentProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/development/projects")
public class DevelopmentProjectController {

    @Autowired
    private DevelopmentProjectService projectService;

    @GetMapping
    public Result<Page<DevelopmentProject>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        
        Page<DevelopmentProject> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<DevelopmentProject> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(DevelopmentProject::getStatus, status);
        }
        queryWrapper.orderByDesc(DevelopmentProject::getCreateTime);
        
        return Result.success(projectService.page(pageParam, queryWrapper));
    }

    @PostMapping
    public Result<Boolean> create(@RequestBody DevelopmentProject project) {
        project.setCreateTime(java.time.LocalDateTime.now());
        project.setUpdateTime(java.time.LocalDateTime.now());
        project.setStatus("PLANNED");
        return Result.success(projectService.save(project));
    }
}
