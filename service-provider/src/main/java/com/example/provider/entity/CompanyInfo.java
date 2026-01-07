package com.example.provider.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业信息实体类
 */
@Data
@TableName("company_info")
public class CompanyInfo {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（关联用户表）
     */
    private Long userId;

    /**
     * 企业全称
     */
    private String companyName;

    /**
     * 统一社会信用代码
     */
    private String creditCode;

    /**
     * 法人代表
     */
    private String legalPerson;

    /**
     * 企业地址
     */
    private String address;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 营业执照URL
     */
    private String licenseUrl;

    /**
     * 注册资本
     */
    private String registeredCapital;

    /**
     * 成立日期
     */
    private LocalDateTime establishDate;

    /**
     * 经营范围
     */
    private String businessScope;

    /**
     * 认证状态：0-未认证，1-审核中，2-已认证，3-已驳回
     */
    private Integer status;

    /**
     * 驳回原因
     */
    private String rejectReason;

    /**
     * 审核人
     */
    private String reviewer;

    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
