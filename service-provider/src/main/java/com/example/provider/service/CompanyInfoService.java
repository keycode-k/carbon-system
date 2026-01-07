package com.example.provider.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.provider.entity.CompanyInfo;

/**
 * 企业信息服务接口
 */
public interface CompanyInfoService extends IService<CompanyInfo> {

    /**
     * 根据用户ID获取企业信息
     * @param userId 用户ID
     * @return 企业信息
     */
    CompanyInfo getByUserId(Long userId);

    /**
     * 提交企业认证
     * @param companyInfo 企业信息
     * @return 是否成功
     */
    boolean submitCertification(CompanyInfo companyInfo);

    /**
     * 审核企业认证
     * @param id 企业信息ID
     * @param status 审核状态（2-通过，3-驳回）
     * @param reviewer 审核人
     * @param rejectReason 驳回原因（审核通过时为空）
     * @return 是否成功
     */
    boolean reviewCertification(Long id, Integer status, String reviewer, String rejectReason);
}
