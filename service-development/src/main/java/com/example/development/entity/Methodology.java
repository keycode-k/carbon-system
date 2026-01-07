package com.example.development.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 方法学实体类
 */
@Data
@TableName("methodology")
public class Methodology {
    
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 方法学编号 (如: CM-001-V01)
     */
    private String code;

    /**
     * 方法学名称
     */
    private String name;

    /**
     * 适用领域 (如: 林业碳汇、可再生能源、节能减排)
     */
    private String category;

    /**
     * 版本号
     */
    private String version;

    /**
     * 方法学描述
     */
    private String description;

    /**
     * 发布机构 (如: 国家发改委、生态环境部)
     */
    private String publisher;

    /**
     * 发布日期
     */
    private LocalDateTime publishDate;

    /**
     * 状态: ACTIVE-有效, DEPRECATED-已废弃, DRAFT-草案
     */
    private String status;

    /**
     * 方法学文件URL
     */
    private String fileUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
