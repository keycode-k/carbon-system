package com.example.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.provider.entity.SysApprovalFlow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 审批流程Mapper接口
 */
@Mapper
public interface SysApprovalFlowMapper extends BaseMapper<SysApprovalFlow> {
    
    /**
     * 根据业务类型查询启用的流程
     */
    @Select("SELECT * FROM sys_approval_flow WHERE business_type = #{businessType} AND status = 1 LIMIT 1")
    SysApprovalFlow selectByBusinessType(String businessType);
    
    /**
     * 根据流程编码查询
     */
    @Select("SELECT * FROM sys_approval_flow WHERE flow_code = #{flowCode} LIMIT 1")
    SysApprovalFlow selectByFlowCode(String flowCode);
}
