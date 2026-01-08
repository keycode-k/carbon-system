package com.example.provider.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典数据实体
 */
@Data
@TableName("sys_dict_data")
public class SysDictData {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 字典类型
     */
    private String dictType;
    
    /**
     * 字典标签（显示名称）
     */
    private String dictLabel;
    
    /**
     * 字典键值（实际值）
     */
    private String dictValue;
    
    /**
     * 字典排序
     */
    private Integer dictSort;
    
    /**
     * 样式属性（CSS类名）
     */
    private String cssClass;
    
    /**
     * 表格回显样式（Element Plus tag类型）
     */
    private String listClass;
    
    /**
     * 是否默认：0-否 1-是
     */
    private Integer isDefault;
    
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
