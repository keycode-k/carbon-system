package com.example.provider.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置实体类
 */
@Data
@TableName("sys_config")
public class SysConfig {
    
    /**
     * 配置ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 配置名称
     */
    private String configName;
    
    /**
     * 配置键名
     */
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
    
    /**
     * 配置类型（1-系统内置 2-自定义）
     */
    private Integer configType;
    
    /**
     * 配置分组
     */
    private String configGroup;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 状态（1-正常 0-停用）
     */
    private Integer status;
    
    /**
     * 创建者
     */
    private String createBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新者
     */
    private String updateBy;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
