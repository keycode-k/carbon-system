package com.example.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.provider.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 系统配置Mapper接口
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {
    
    /**
     * 根据配置键名查询配置值
     */
    @Select("SELECT config_value FROM sys_config WHERE config_key = #{configKey} AND status = 1 LIMIT 1")
    String selectConfigValueByKey(String configKey);
}
