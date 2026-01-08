package com.example.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.provider.entity.SysApprovalHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审批历史Mapper接口
 */
@Mapper
public interface SysApprovalHistoryMapper extends BaseMapper<SysApprovalHistory> {
    
    /**
     * 根据审批记录ID查询审批历史
     */
    @Select("SELECT * FROM sys_approval_history WHERE record_id = #{recordId} ORDER BY approve_time ASC")
    List<SysApprovalHistory> selectByRecordId(Long recordId);
}
