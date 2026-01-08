package com.example.provider.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批记录实体类（具体业务的审批实例）
 */
@Data
@TableName("sys_approval_record")
public class SysApprovalRecord {
    
    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 审批流程ID
     */
    private Long flowId;
    
    /**
     * 业务类型
     */
    private String businessType;
    
    /**
     * 业务ID（关联具体业务表的ID）
     */
    private Long businessId;
    
    /**
     * 业务标题
     */
    private String businessTitle;
    
    /**
     * 申请人ID
     */
    private Long applicantId;
    
    /**
     * 申请人名称
     */
    private String applicantName;
    
    /**
     * 当前节点ID
     */
    private Long currentNodeId;
    
    /**
     * 当前节点名称
     */
    private String currentNodeName;
    
    /**
     * 审批状态（0-待审批 1-审批中 2-已通过 3-已驳回 4-已撤回）
     */
    private Integer status;
    
    /**
     * 申请时间
     */
    private LocalDateTime applyTime;
    
    /**
     * 完成时间
     */
    private LocalDateTime completeTime;
    
    /**
     * 备注
     */
    private String remark;
}
