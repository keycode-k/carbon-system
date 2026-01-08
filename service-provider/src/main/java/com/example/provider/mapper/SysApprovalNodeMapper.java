package com.example.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.provider.entity.SysApprovalNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审批节点Mapper接口
 */
@Mapper
public interface SysApprovalNodeMapper extends BaseMapper<SysApprovalNode> {
    
    /**
     * 根据流程ID查询节点列表（按序号排序）
     */
    @Select("SELECT * FROM sys_approval_node WHERE flow_id = #{flowId} AND status = 1 ORDER BY node_order ASC")
    List<SysApprovalNode> selectByFlowId(Long flowId);
    
    /**
     * 获取流程的第一个节点
     */
    @Select("SELECT * FROM sys_approval_node WHERE flow_id = #{flowId} AND status = 1 ORDER BY node_order ASC LIMIT 1")
    SysApprovalNode selectFirstNode(Long flowId);
    
    /**
     * 获取下一个节点
     */
    @Select("SELECT * FROM sys_approval_node WHERE flow_id = #{flowId} AND node_order > #{currentOrder} AND status = 1 ORDER BY node_order ASC LIMIT 1")
    SysApprovalNode selectNextNode(Long flowId, Integer currentOrder);
}
