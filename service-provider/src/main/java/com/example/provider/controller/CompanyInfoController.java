package com.example.provider.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.model.Result;
import com.example.provider.entity.CompanyInfo;
import com.example.provider.service.CompanyInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 企业信息控制器
 */
@RestController
@RequestMapping("/api/company")
public class CompanyInfoController {

    @Autowired
    private CompanyInfoService companyInfoService;

    /**
     * 根据用户ID获取企业信息
     */
    @GetMapping("/info/{userId}")
    public Result<CompanyInfo> getByUserId(@PathVariable Long userId) {
        CompanyInfo companyInfo = companyInfoService.getByUserId(userId);
        return Result.success(companyInfo);
    }

    /**
     * 提交企业认证
     */
    @PostMapping("/submit")
    public Result<String> submitCertification(@RequestBody CompanyInfo companyInfo) {
        boolean success = companyInfoService.submitCertification(companyInfo);
        return success ? Result.success("提交成功，等待审核") : Result.error("提交失败");
    }

    /**
     * 保存草稿（不提交审核）
     */
    @PostMapping("/draft")
    public Result<String> saveDraft(@RequestBody CompanyInfo companyInfo) {
        // 设置状态为未认证
        companyInfo.setStatus(0);
        
        CompanyInfo existing = companyInfoService.getByUserId(companyInfo.getUserId());
        boolean success;
        if (existing != null) {
            companyInfo.setId(existing.getId());
            success = companyInfoService.updateById(companyInfo);
        } else {
            success = companyInfoService.save(companyInfo);
        }
        
        return success ? Result.success("草稿保存成功") : Result.error("保存失败");
    }

    /**
     * 更新企业信息
     */
    @PutMapping("/{id}")
    public Result<String> updateCompany(@PathVariable Long id, @RequestBody CompanyInfo companyInfo) {
        companyInfo.setId(id);
        boolean success = companyInfoService.updateById(companyInfo);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除企业信息
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteCompany(@PathVariable Long id) {
        boolean success = companyInfoService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 分页查询企业列表（管理员使用）
     */
    @GetMapping("/list")
    public Result<Page<CompanyInfo>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) Integer status) {
        
        Page<CompanyInfo> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CompanyInfo> wrapper = new LambdaQueryWrapper<>();
        
        if (companyName != null && !companyName.isEmpty()) {
            wrapper.like(CompanyInfo::getCompanyName, companyName);
        }
        if (status != null) {
            wrapper.eq(CompanyInfo::getStatus, status);
        }
        
        wrapper.orderByDesc(CompanyInfo::getCreateTime);
        Page<CompanyInfo> result = companyInfoService.page(pageParam, wrapper);
        
        return Result.success(result);
    }

    /**
     * 审核企业认证（管理员使用）
     */
    @PostMapping("/review")
    public Result<String> reviewCertification(
            @RequestParam Long id,
            @RequestParam Integer status,
            @RequestParam String reviewer,
            @RequestParam(required = false) String rejectReason) {
        
        if (status != 2 && status != 3) {
            return Result.error("状态参数错误");
        }
        
        boolean success = companyInfoService.reviewCertification(id, status, reviewer, rejectReason);
        return success ? Result.success("审核完成") : Result.error("审核失败");
    }

    /**
     * 获取待审核企业数量（管理员使用）
     */
    @GetMapping("/pending/count")
    public Result<Long> getPendingCount() {
        LambdaQueryWrapper<CompanyInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompanyInfo::getStatus, 1);
        long count = companyInfoService.count(wrapper);
        return Result.success(count);
    }
}
