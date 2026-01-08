package com.example.development.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.model.Result;
import com.example.development.entity.DevelopmentProject;
import com.example.development.entity.Methodology;
import com.example.development.service.DevelopmentProjectService;
import com.example.development.service.MethodologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 开发项目管理控制器
 */
@RestController
@RequestMapping("/api/development/projects")
public class DevelopmentProjectController {

    @Autowired
    private DevelopmentProjectService projectService;
    
    @Autowired
    private MethodologyService methodologyService;

    /**
     * 分页查询项目列表
     */
    @GetMapping
    public Result<Page<DevelopmentProject>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long methodologyId,
            @RequestParam(required = false) String status) {
        
        Page<DevelopmentProject> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<DevelopmentProject> queryWrapper = new LambdaQueryWrapper<>();
        
        // 名称模糊查询
        if (StringUtils.hasText(name)) {
            queryWrapper.like(DevelopmentProject::getName, name);
        }
        
        // 方法学筛选
        if (methodologyId != null) {
            queryWrapper.eq(DevelopmentProject::getMethodologyId, methodologyId);
        }
        
        // 状态筛选
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(DevelopmentProject::getStatus, status);
        }
        
        queryWrapper.orderByDesc(DevelopmentProject::getCreateTime);
        
        Page<DevelopmentProject> result = projectService.page(pageParam, queryWrapper);
        
        // 填充方法学名称
        for (DevelopmentProject project : result.getRecords()) {
            if (project.getMethodologyId() != null) {
                Methodology methodology = methodologyService.getById(project.getMethodologyId());
                if (methodology != null) {
                    project.setMethodologyName(methodology.getName());
                    project.setMethodologyCode(methodology.getCode());
                }
            }
        }
        
        return Result.success(result);
    }
    
    /**
     * 查询所有项目（不分页，用于下拉选择）
     */
    @GetMapping("/all")
    public Result<List<DevelopmentProject>> listAll(
            @RequestParam(required = false) String status) {
        
        LambdaQueryWrapper<DevelopmentProject> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(DevelopmentProject::getStatus, status);
        }
        queryWrapper.orderByDesc(DevelopmentProject::getCreateTime);
        
        List<DevelopmentProject> list = projectService.list(queryWrapper);
        
        // 填充方法学名称
        for (DevelopmentProject project : list) {
            if (project.getMethodologyId() != null) {
                Methodology methodology = methodologyService.getById(project.getMethodologyId());
                if (methodology != null) {
                    project.setMethodologyName(methodology.getName());
                    project.setMethodologyCode(methodology.getCode());
                }
            }
        }
        
        return Result.success(list);
    }
    
    /**
     * 根据ID查询项目详情
     */
    @GetMapping("/{id}")
    public Result<DevelopmentProject> getById(@PathVariable Long id) {
        DevelopmentProject project = projectService.getById(id);
        if (project == null) {
            return Result.error("项目不存在");
        }
        
        // 填充方法学名称
        if (project.getMethodologyId() != null) {
            Methodology methodology = methodologyService.getById(project.getMethodologyId());
            if (methodology != null) {
                project.setMethodologyName(methodology.getName());
                project.setMethodologyCode(methodology.getCode());
            }
        }
        
        return Result.success(project);
    }

    /**
     * 新增项目
     */
    @PostMapping
    public Result<Boolean> create(@RequestBody DevelopmentProject project) {
        project.setCreateTime(LocalDateTime.now());
        project.setUpdateTime(LocalDateTime.now());
        if (project.getStatus() == null) {
            project.setStatus("PLANNED");
        }
        if (project.getCurrentStep() == null) {
            project.setCurrentStep(0);
        }
        return Result.success(projectService.save(project));
    }
    
    /**
     * 更新项目
     */
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody DevelopmentProject project) {
        project.setId(id);
        project.setUpdateTime(LocalDateTime.now());
        return Result.success(projectService.updateById(project));
    }
    
    /**
     * 删除项目
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(projectService.removeById(id));
    }
    
    /**
     * 更新项目阶段
     */
    @PutMapping("/{id}/step")
    public Result<Boolean> updateStep(
            @PathVariable Long id, 
            @RequestParam Integer step) {
        
        DevelopmentProject project = projectService.getById(id);
        if (project == null) {
            return Result.error("项目不存在");
        }
        
        project.setCurrentStep(step);
        project.setUpdateTime(LocalDateTime.now());
        
        // 根据阶段自动更新状态
        if (step >= 3) {
            project.setStatus("REGISTERED");
        }
        if (step >= 4) {
            project.setStatus("VERIFIED");
        }
        if (step >= 5) {
            project.setStatus("ISSUED");
        }
        
        return Result.success(projectService.updateById(project));
    }
}
