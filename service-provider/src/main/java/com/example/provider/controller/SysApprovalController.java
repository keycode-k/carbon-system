package com.example.provider.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.annotation.OperationLog;
import com.example.common.annotation.OperationLog.OperationType;
import com.example.common.annotation.RequireLogin;
import com.example.common.annotation.RequirePermission;
import com.example.common.model.Result;
import com.example.common.security.LoginUser;
import com.example.common.security.UserContextHolder;
import com.example.provider.entity.*;
import com.example.provider.service.SysApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审批流程管理Controller
 */
@RestController
@RequestMapping("/api/system/approval")
@RequireLogin
public class SysApprovalController {
    
    @Autowired
    private SysApprovalService approvalService;
    
    // ========== 流程定义管理 ==========
    
    /**
     * 分页查询流程定义
     */
    @GetMapping("/flow/list")
    @RequirePermission("system:approval:list")
    public Result<Page<SysApprovalFlow>> listFlows(
            @RequestParam(required = false) String flowName,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<SysApprovalFlow> result = approvalService.pageFlows(flowName, businessType, status, page, size);
        return Result.success(result);
    }
    
    /**
     * 获取流程详情（包含节点）
     */
    @GetMapping("/flow/{id}")
    @RequirePermission("system:approval:query")
    public Result<Map<String, Object>> getFlow(@PathVariable Long id) {
        SysApprovalFlow flow = approvalService.getFlowById(id);
        if (flow == null) {
            return Result.error("流程不存在");
        }
        
        List<SysApprovalNode> nodes = approvalService.getNodesByFlowId(id);
        
        Map<String, Object> data = new HashMap<>();
        data.put("flow", flow);
        data.put("nodes", nodes);
        
        return Result.success(data);
    }
    
    /**
     * 创建流程
     */
    @PostMapping("/flow")
    @RequirePermission("system:approval:add")
    @OperationLog(module = "审批管理", type = OperationType.CREATE, description = "创建审批流程")
    public Result<Object> createFlow(@RequestBody SysApprovalFlow flow) {
        LoginUser user = UserContextHolder.getUser();
        if (user != null) {
            flow.setCreateBy(user.getUsername());
        }
        
        boolean success = approvalService.createFlow(flow);
        if (success) {
            return Result.success("创建成功", null);
        }
        return Result.error("创建失败，流程编码可能已存在");
    }
    
    /**
     * 更新流程
     */
    @PutMapping("/flow")
    @RequirePermission("system:approval:edit")
    @OperationLog(module = "审批管理", type = OperationType.UPDATE, description = "更新审批流程")
    public Result<Object> updateFlow(@RequestBody SysApprovalFlow flow) {
        if (flow.getId() == null) {
            return Result.error("流程ID不能为空");
        }
        
        LoginUser user = UserContextHolder.getUser();
        if (user != null) {
            flow.setUpdateBy(user.getUsername());
        }
        
        boolean success = approvalService.updateFlow(flow);
        if (success) {
            return Result.success("更新成功", null);
        }
        return Result.error("更新失败");
    }
    
    /**
     * 删除流程
     */
    @DeleteMapping("/flow/{id}")
    @RequirePermission("system:approval:delete")
    @OperationLog(module = "审批管理", type = OperationType.DELETE, description = "删除审批流程")
    public Result<Object> deleteFlow(@PathVariable Long id) {
        boolean success = approvalService.deleteFlow(id);
        if (success) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
    
    // ========== 流程节点管理 ==========
    
    /**
     * 获取流程节点
     */
    @GetMapping("/flow/{flowId}/nodes")
    @RequirePermission("system:approval:query")
    public Result<List<SysApprovalNode>> getFlowNodes(@PathVariable Long flowId) {
        List<SysApprovalNode> nodes = approvalService.getNodesByFlowId(flowId);
        return Result.success(nodes);
    }
    
    /**
     * 保存流程节点
     */
    @PostMapping("/flow/{flowId}/nodes")
    @RequirePermission("system:approval:edit")
    @OperationLog(module = "审批管理", type = OperationType.UPDATE, description = "配置审批节点")
    public Result<Object> saveFlowNodes(
            @PathVariable Long flowId,
            @RequestBody List<SysApprovalNode> nodes) {
        
        boolean success = approvalService.saveNodes(flowId, nodes);
        if (success) {
            return Result.success("保存成功", null);
        }
        return Result.error("保存失败");
    }
    
    // ========== 审批业务 ==========
    
    /**
     * 分页查询审批记录
     */
    @GetMapping("/record/list")
    @RequirePermission("system:approval:list")
    public Result<Page<SysApprovalRecord>> listRecords(
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String businessTitle,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long applicantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<SysApprovalRecord> result = approvalService.pageRecords(
                businessType, businessTitle, status, applicantId, page, size);
        return Result.success(result);
    }
    
    /**
     * 获取审批记录详情
     */
    @GetMapping("/record/{id}")
    @RequirePermission("system:approval:query")
    public Result<Map<String, Object>> getRecord(@PathVariable Long id) {
        SysApprovalRecord record = approvalService.getRecordById(id);
        if (record == null) {
            return Result.error("审批记录不存在");
        }
        
        List<SysApprovalHistory> histories = approvalService.getApprovalHistory(id);
        
        Map<String, Object> data = new HashMap<>();
        data.put("record", record);
        data.put("histories", histories);
        
        return Result.success(data);
    }
    
    /**
     * 获取待我审批的记录
     */
    @GetMapping("/pending")
    public Result<List<SysApprovalRecord>> getPendingApprovals() {
        LoginUser user = UserContextHolder.getUser();
        if (user == null) {
            return Result.error("用户未登录");
        }
        
        List<SysApprovalRecord> records = approvalService.getMyPendingApprovals(user.getUserId());
        return Result.success(records);
    }
    
    /**
     * 获取我发起的审批
     */
    @GetMapping("/my-applied")
    public Result<Page<SysApprovalRecord>> getMyAppliedRecords(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        LoginUser user = UserContextHolder.getUser();
        if (user == null) {
            return Result.error("用户未登录");
        }
        
        Page<SysApprovalRecord> result = approvalService.getMyAppliedRecords(user.getUserId(), status, page, size);
        return Result.success(result);
    }
    
    /**
     * 审批通过
     */
    @PostMapping("/approve/{recordId}")
    @OperationLog(module = "审批管理", type = OperationType.UPDATE, description = "审批通过")
    public Result<Object> approve(
            @PathVariable Long recordId,
            @RequestBody Map<String, String> params) {
        
        LoginUser user = UserContextHolder.getUser();
        if (user == null) {
            return Result.error("用户未登录");
        }
        
        String comment = params.get("comment");
        
        try {
            boolean success = approvalService.approve(recordId, user.getUserId(), user.getUsername(), comment);
            if (success) {
                return Result.success("审批成功", null);
            }
            return Result.error("审批失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 审批驳回
     */
    @PostMapping("/reject/{recordId}")
    @OperationLog(module = "审批管理", type = OperationType.UPDATE, description = "审批驳回")
    public Result<Object> reject(
            @PathVariable Long recordId,
            @RequestBody Map<String, String> params) {
        
        LoginUser user = UserContextHolder.getUser();
        if (user == null) {
            return Result.error("用户未登录");
        }
        
        String comment = params.get("comment");
        if (comment == null || comment.isEmpty()) {
            return Result.error("驳回原因不能为空");
        }
        
        try {
            boolean success = approvalService.reject(recordId, user.getUserId(), user.getUsername(), comment);
            if (success) {
                return Result.success("驳回成功", null);
            }
            return Result.error("驳回失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 撤回审批
     */
    @PostMapping("/withdraw/{recordId}")
    @OperationLog(module = "审批管理", type = OperationType.UPDATE, description = "撤回审批")
    public Result<Object> withdraw(@PathVariable Long recordId) {
        LoginUser user = UserContextHolder.getUser();
        if (user == null) {
            return Result.error("用户未登录");
        }
        
        try {
            boolean success = approvalService.withdraw(recordId, user.getUserId());
            if (success) {
                return Result.success("撤回成功", null);
            }
            return Result.error("撤回失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
