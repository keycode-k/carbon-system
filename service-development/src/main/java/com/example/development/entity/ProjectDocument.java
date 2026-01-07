package com.example.development.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目文档实体类
 */
@Data
@TableName("project_document")
public class ProjectDocument {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 项目ID
     */
    private Long projectId;
    
    /**
     * 文档名称
     */
    private String docName;
    
    /**
     * 文档类型(设计文档/监测报告/核查报告)
     */
    private String docType;
    
    /**
     * 文件URL
     */
    private String fileUrl;
    
    /**
     * 文件大小(bytes)
     */
    private Long fileSize;
    
    /**
     * 版本号
     */
    private String version;
    
    /**
     * 上传人ID
     */
    private Long uploadUserId;
    
    /**
     * 状态(DRAFT-草稿/REVIEWING-审核中/APPROVED-已批准)
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
