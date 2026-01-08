package com.example.provider.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批流程节点实体类
 */
@Data
@TableName("sys_approval_node")
public class SysApprovalNode {
    
    /**
     * 节点ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 所属流程ID
     */
    private Long flowId;
    
    /**
     * 节点名称
     */
    private String nodeName;
    
    /**
     * 节点序号
     */
    private Integer nodeOrder;
    
    /**
     * 审批类型（1-角色审批 2-指定用户 3-部门负责人）
     */
    private Integer approverType;
    
    /**
     * 审批人ID/角色ID（根据approverType确定）
     */
    private Long approverId;
    
    /**
     * 审批人名称
     */
    private String approverName;
    
    /**
     * 是否允许转交（1-是 0-否）
     */
    private Integer allowTransfer;
    
    /**
     * 是否允许驳回（1-是 0-否）
     */
    private Integer allowReject;
    
    /**
     * 状态（1-启用 0-停用）
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
