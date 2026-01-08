package com.example.provider.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.provider.entity.*;
import com.example.provider.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批流程服务类
 */
@Service
public class SysApprovalService {
    
    @Autowired
    private SysApprovalFlowMapper flowMapper;
    
    @Autowired
    private SysApprovalNodeMapper nodeMapper;
    
    @Autowired
    private SysApprovalRecordMapper recordMapper;
    
    @Autowired
    private SysApprovalHistoryMapper historyMapper;
    
    // ========== 流程定义管理 ==========
    
    /**
     * 分页查询流程定义
     */
    public Page<SysApprovalFlow> pageFlows(String flowName, String businessType, Integer status, int page, int size) {
        LambdaQueryWrapper<SysApprovalFlow> queryWrapper = new LambdaQueryWrapper<>();
        
        if (flowName != null && !flowName.isEmpty()) {
            queryWrapper.like(SysApprovalFlow::getFlowName, flowName);
        }
        if (businessType != null && !businessType.isEmpty()) {
            queryWrapper.eq(SysApprovalFlow::getBusinessType, businessType);
        }
        if (status != null) {
            queryWrapper.eq(SysApprovalFlow::getStatus, status);
        }
        queryWrapper.orderByDesc(SysApprovalFlow::getId);
        
        return flowMapper.selectPage(new Page<>(page, size), queryWrapper);
    }
    
    /**
     * 获取流程详情
     */
    public SysApprovalFlow getFlowById(Long id) {
        return flowMapper.selectById(id);
    }
    
    /**
     * 创建流程
     */
    @Transactional
    public boolean createFlow(SysApprovalFlow flow) {
        // 检查流程编码是否已存在
        if (flowMapper.selectByFlowCode(flow.getFlowCode()) != null) {
            return false;
        }
        
        flow.setCreateTime(LocalDateTime.now());
        if (flow.getStatus() == null) {
            flow.setStatus(1);
        }
        return flowMapper.insert(flow) > 0;
    }
    
    /**
     * 更新流程
     */
    public boolean updateFlow(SysApprovalFlow flow) {
        flow.setUpdateTime(LocalDateTime.now());
        return flowMapper.updateById(flow) > 0;
    }
    
    /**
     * 删除流程
     */
    @Transactional
    public boolean deleteFlow(Long id) {
        // 删除流程节点
        LambdaQueryWrapper<SysApprovalNode> nodeWrapper = new LambdaQueryWrapper<>();
        nodeWrapper.eq(SysApprovalNode::getFlowId, id);
        nodeMapper.delete(nodeWrapper);
        
        return flowMapper.deleteById(id) > 0;
    }
    
    // ========== 流程节点管理 ==========
    
    /**
     * 获取流程的所有节点
     */
    public List<SysApprovalNode> getNodesByFlowId(Long flowId) {
        return nodeMapper.selectByFlowId(flowId);
    }
    
    /**
     * 保存流程节点（先删除旧的，再插入新的）
     */
    @Transactional
    public boolean saveNodes(Long flowId, List<SysApprovalNode> nodes) {
        // 删除旧节点
        LambdaQueryWrapper<SysApprovalNode> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SysApprovalNode::getFlowId, flowId);
        nodeMapper.delete(deleteWrapper);
        
        // 插入新节点
        for (int i = 0; i < nodes.size(); i++) {
            SysApprovalNode node = nodes.get(i);
            node.setFlowId(flowId);
            node.setNodeOrder(i + 1);
            node.setCreateTime(LocalDateTime.now());
            if (node.getStatus() == null) {
                node.setStatus(1);
            }
            nodeMapper.insert(node);
        }
        return true;
    }
    
    // ========== 审批业务 ==========
    
    /**
     * 发起审批
     */
    @Transactional
    public SysApprovalRecord startApproval(String businessType, Long businessId, String businessTitle,
                                           Long applicantId, String applicantName, String remark) {
        // 查找对应的审批流程
        SysApprovalFlow flow = flowMapper.selectByBusinessType(businessType);
        if (flow == null) {
            throw new RuntimeException("未找到对应的审批流程配置");
        }
        
        // 获取第一个节点
        SysApprovalNode firstNode = nodeMapper.selectFirstNode(flow.getId());
        if (firstNode == null) {
            throw new RuntimeException("审批流程未配置节点");
        }
        
        // 创建审批记录
        SysApprovalRecord record = new SysApprovalRecord();
        record.setFlowId(flow.getId());
        record.setBusinessType(businessType);
        record.setBusinessId(businessId);
        record.setBusinessTitle(businessTitle);
        record.setApplicantId(applicantId);
        record.setApplicantName(applicantName);
        record.setCurrentNodeId(firstNode.getId());
        record.setCurrentNodeName(firstNode.getNodeName());
        record.setStatus(0); // 待审批
        record.setApplyTime(LocalDateTime.now());
        record.setRemark(remark);
        
        recordMapper.insert(record);
        return record;
    }
    
    /**
     * 审批通过
     */
    @Transactional
    public boolean approve(Long recordId, Long approverId, String approverName, String comment) {
        SysApprovalRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("审批记录不存在");
        }
        
        SysApprovalNode currentNode = nodeMapper.selectById(record.getCurrentNodeId());
        if (currentNode == null) {
            throw new RuntimeException("当前节点不存在");
        }
        
        // 记录审批历史
        SysApprovalHistory history = new SysApprovalHistory();
        history.setRecordId(recordId);
        history.setNodeId(currentNode.getId());
        history.setNodeName(currentNode.getNodeName());
        history.setApproverId(approverId);
        history.setApproverName(approverName);
        history.setAction(1); // 通过
        history.setComment(comment);
        history.setApproveTime(LocalDateTime.now());
        historyMapper.insert(history);
        
        // 查找下一个节点
        SysApprovalNode nextNode = nodeMapper.selectNextNode(record.getFlowId(), currentNode.getNodeOrder());
        
        if (nextNode == null) {
            // 没有下一个节点，审批完成
            record.setStatus(2); // 已通过
            record.setCompleteTime(LocalDateTime.now());
        } else {
            // 进入下一个节点
            record.setCurrentNodeId(nextNode.getId());
            record.setCurrentNodeName(nextNode.getNodeName());
            record.setStatus(1); // 审批中
        }
        
        return recordMapper.updateById(record) > 0;
    }
    
    /**
     * 审批驳回
     */
    @Transactional
    public boolean reject(Long recordId, Long approverId, String approverName, String comment) {
        SysApprovalRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("审批记录不存在");
        }
        
        SysApprovalNode currentNode = nodeMapper.selectById(record.getCurrentNodeId());
        
        // 记录审批历史
        SysApprovalHistory history = new SysApprovalHistory();
        history.setRecordId(recordId);
        history.setNodeId(record.getCurrentNodeId());
        history.setNodeName(currentNode != null ? currentNode.getNodeName() : "未知节点");
        history.setApproverId(approverId);
        history.setApproverName(approverName);
        history.setAction(2); // 驳回
        history.setComment(comment);
        history.setApproveTime(LocalDateTime.now());
        historyMapper.insert(history);
        
        // 更新审批状态
        record.setStatus(3); // 已驳回
        record.setCompleteTime(LocalDateTime.now());
        
        return recordMapper.updateById(record) > 0;
    }
    
    /**
     * 撤回审批
     */
    @Transactional
    public boolean withdraw(Long recordId, Long applicantId) {
        SysApprovalRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("审批记录不存在");
        }
        
        // 只有申请人本人且审批未完成时可以撤回
        if (!record.getApplicantId().equals(applicantId)) {
            throw new RuntimeException("只有申请人可以撤回");
        }
        if (record.getStatus() >= 2) {
            throw new RuntimeException("审批已完成，无法撤回");
        }
        
        record.setStatus(4); // 已撤回
        record.setCompleteTime(LocalDateTime.now());
        
        return recordMapper.updateById(record) > 0;
    }
    
    // ========== 查询 ==========
    
    /**
     * 分页查询审批记录
     */
    public Page<SysApprovalRecord> pageRecords(String businessType, String businessTitle, 
                                               Integer status, Long applicantId,
                                               int page, int size) {
        LambdaQueryWrapper<SysApprovalRecord> queryWrapper = new LambdaQueryWrapper<>();
        
        if (businessType != null && !businessType.isEmpty()) {
            queryWrapper.eq(SysApprovalRecord::getBusinessType, businessType);
        }
        if (businessTitle != null && !businessTitle.isEmpty()) {
            queryWrapper.like(SysApprovalRecord::getBusinessTitle, businessTitle);
        }
        if (status != null) {
            queryWrapper.eq(SysApprovalRecord::getStatus, status);
        }
        if (applicantId != null) {
            queryWrapper.eq(SysApprovalRecord::getApplicantId, applicantId);
        }
        queryWrapper.orderByDesc(SysApprovalRecord::getApplyTime);
        
        return recordMapper.selectPage(new Page<>(page, size), queryWrapper);
    }
    
    /**
     * 获取审批记录详情
     */
    public SysApprovalRecord getRecordById(Long id) {
        return recordMapper.selectById(id);
    }
    
    /**
     * 获取审批历史
     */
    public List<SysApprovalHistory> getApprovalHistory(Long recordId) {
        return historyMapper.selectByRecordId(recordId);
    }
    
    /**
     * 查询待我审批的记录
     */
    public List<SysApprovalRecord> getMyPendingApprovals(Long approverId) {
        return recordMapper.selectPendingByApprover(approverId);
    }
    
    /**
     * 查询我发起的审批
     */
    public Page<SysApprovalRecord> getMyAppliedRecords(Long applicantId, Integer status, int page, int size) {
        LambdaQueryWrapper<SysApprovalRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysApprovalRecord::getApplicantId, applicantId);
        if (status != null) {
            queryWrapper.eq(SysApprovalRecord::getStatus, status);
        }
        queryWrapper.orderByDesc(SysApprovalRecord::getApplyTime);
        
        return recordMapper.selectPage(new Page<>(page, size), queryWrapper);
    }
}
