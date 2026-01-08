package com.example.development.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("development_project")
public class DevelopmentProject {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name; // 项目名称
    
    private String projectType; // 项目类型(风电/光伏/林业碳汇等)

    private Long methodologyId; // 方法学ID
    
    @TableField(exist = false)
    private String methodologyName; // 方法学名称(非数据库字段)
    
    @TableField(exist = false)
    private String methodologyCode; // 方法学编号(非数据库字段)

    private String status; // 状态: PLANNED, REGISTERED, VERIFIED, ISSUED
    
    private Integer currentStep; // 当前阶段(0-5)

    private Long ownerId; // 业主ID

    private String location; // 项目地点

    private Double estimatedEmissionReduction; // 预计减排量
    
    private String description; // 项目描述

    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
