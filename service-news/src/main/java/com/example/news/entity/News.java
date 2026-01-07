package com.example.news.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 新闻资讯实体类
 */
@Data
@TableName("news")
public class News {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 新闻标题
     */
    private String title;
    
    /**
     * 新闻内容（支持HTML或Markdown）
     */
    private String content;
    
    /**
     * 新闻摘要
     */
    private String summary;
    
    /**
     * 新闻分类（政策法规/行业动态/技术创新/市场分析）
     */
    private String category;
    
    /**
     * 作者
     */
    private String author;
    
    /**
     * 来源
     */
    private String source;
    
    /**
     * 封面图片URL
     */
    private String coverImage;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 状态：0-草稿 1-已发布 2-已下线
     */
    private Integer status;
    
    /**
     * 是否置顶：0-否 1-是
     */
    private Integer isTop;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
    
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
     * 逻辑删除：0-正常 1-已删除
     */
    @TableLogic
    private Integer deleted;
}
