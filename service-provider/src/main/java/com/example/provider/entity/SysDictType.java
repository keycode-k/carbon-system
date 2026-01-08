package com.example.provider.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典类型实体
 */
@Data
@TableName("sys_dict_type")
public class SysDictType {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 字典名称
     */
    private String dictName;
    
    /**
     * 字典类型（唯一标识）
     */
    private String dictType;
    
    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;
    
    /**
     * 备注说明
     */
    private String remark;
    
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
