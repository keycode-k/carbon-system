package com.example.provider.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批历史实体类（记录每一步审批的详细信息）
 */
@Data
@TableName("sys_approval_history")
public class SysApprovalHistory {
    
    /**
     * 历史ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 审批记录ID
     */
    private Long recordId;
    
    /**
     * 审批节点ID
     */
    private Long nodeId;
    
    /**
     * 节点名称
     */
    private String nodeName;
    
    /**
     * 审批人ID
     */
    private Long approverId;
    
    /**
     * 审批人名称
     */
    private String approverName;
    
    /**
     * 审批动作（1-通过 2-驳回 3-转交）
     */
    private Integer action;
    
    /**
     * 审批意见
     */
    private String comment;
    
    /**
     * 转交目标用户ID
     */
    private Long transferToUserId;
    
    /**
     * 转交目标用户名称
     */
    private String transferToUserName;
    
    /**
     * 审批时间
     */
    private LocalDateTime approveTime;
}
