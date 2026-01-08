package com.example.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.provider.entity.SysApprovalRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审批记录Mapper接口
 */
@Mapper
public interface SysApprovalRecordMapper extends BaseMapper<SysApprovalRecord> {
    
    /**
     * 根据业务ID和业务类型查询审批记录
     */
    @Select("SELECT * FROM sys_approval_record WHERE business_type = #{businessType} AND business_id = #{businessId} ORDER BY apply_time DESC LIMIT 1")
    SysApprovalRecord selectByBusiness(String businessType, Long businessId);
    
    /**
     * 查询待我审批的记录
     */
    @Select("SELECT r.* FROM sys_approval_record r " +
            "INNER JOIN sys_approval_node n ON r.current_node_id = n.id " +
            "WHERE r.status IN (0, 1) AND n.approver_id = #{approverId} " +
            "ORDER BY r.apply_time DESC")
    List<SysApprovalRecord> selectPendingByApprover(Long approverId);
}
