package com.example.provider.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统角色实体
 */
@Data
@TableName("sys_role")
public class SysRole {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 角色名称
     */
    private String roleName;
    
    /**
     * 角色编码（唯一标识）
     */
    private String roleCode;
    
    /**
     * 角色描述
     */
    private String description;
    
    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
