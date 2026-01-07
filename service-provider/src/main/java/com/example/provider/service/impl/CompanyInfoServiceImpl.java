package com.example.provider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.provider.entity.CompanyInfo;
import com.example.provider.mapper.CompanyInfoMapper;
import com.example.provider.service.CompanyInfoService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 企业信息服务实现类
 */
@Service
public class CompanyInfoServiceImpl extends ServiceImpl<CompanyInfoMapper, CompanyInfo> implements CompanyInfoService {

    @Override
    public CompanyInfo getByUserId(Long userId) {
        LambdaQueryWrapper<CompanyInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompanyInfo::getUserId, userId);
        return this.getOne(wrapper);
    }

    @Override
    public boolean submitCertification(CompanyInfo companyInfo) {
        // 设置状态为审核中
        companyInfo.setStatus(1);
        companyInfo.setCreateTime(LocalDateTime.now());
        companyInfo.setUpdateTime(LocalDateTime.now());
        
        // 检查用户是否已有企业信息
        CompanyInfo existing = getByUserId(companyInfo.getUserId());
        if (existing != null) {
            // 更新现有记录
            companyInfo.setId(existing.getId());
            return this.updateById(companyInfo);
        } else {
            // 新增记录
            return this.save(companyInfo);
        }
    }

    @Override
    public boolean reviewCertification(Long id, Integer status, String reviewer, String rejectReason) {
        CompanyInfo companyInfo = this.getById(id);
        if (companyInfo == null) {
            return false;
        }
        
        companyInfo.setStatus(status);
        companyInfo.setReviewer(reviewer);
        companyInfo.setReviewTime(LocalDateTime.now());
        companyInfo.setUpdateTime(LocalDateTime.now());
        
        if (status == 3) {
            // 驳回时设置驳回原因
            companyInfo.setRejectReason(rejectReason);
        } else {
            // 通过时清空驳回原因
            companyInfo.setRejectReason(null);
        }
        
        return this.updateById(companyInfo);
    }
}
