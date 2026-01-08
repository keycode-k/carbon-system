package com.example.provider.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统菜单/权限实体
 */
@Data
@TableName("sys_menu")
public class SysMenu {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 父菜单ID，顶级菜单为0
     */
    private Long parentId;
    
    /**
     * 菜单名称
     */
    private String menuName;
    
    /**
     * 路由路径
     */
    private String menuPath;
    
    /**
     * 组件路径
     */
    private String component;
    
    /**
     * 权限标识（如：system:user:list）
     */
    private String permission;
    
    /**
     * 菜单类型：M-目录 C-菜单 F-按钮/功能
     */
    private String menuType;
    
    /**
     * 菜单图标
     */
    private String icon;
    
    /**
     * 排序号
     */
    private Integer orderNum;
    
    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;
    
    /**
     * 是否显示：0-隐藏 1-显示
     */
    private Integer visible;
    
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
    
    /**
     * 子菜单列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<SysMenu> children;
}
