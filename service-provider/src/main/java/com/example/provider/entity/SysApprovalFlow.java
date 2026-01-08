package com.example.provider.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批流程定义实体类
 */
@Data
@TableName("sys_approval_flow")
public class SysApprovalFlow {
    
    /**
     * 流程ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 流程名称
     */
    private String flowName;
    
    /**
     * 流程编码
     */
    private String flowCode;
    
    /**
     * 业务类型（如：ASSET_ISSUE-资产发行，TRADE_LARGE-大额交易）
     */
    private String businessType;
    
    /**
     * 流程描述
     */
    private String description;
    
    /**
     * 状态（1-启用 0-停用）
     */
    private Integer status;
    
    /**
     * 创建者
     */
    private String createBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新者
     */
    private String updateBy;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
