package com.example.development.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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

    private String methodology; // 方法学

    private String status; // 状态: PLANNED, REGISTERED, VERIFIED, ISSUED

    private Long ownerId; // 业主ID

    private String location; // 项目地点

    private Double estimatedEmissionReduction; // 预计减排量

    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
