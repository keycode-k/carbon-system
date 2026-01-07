package com.example.news.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.news.entity.News;

/**
 * 新闻资讯服务接口
 */
public interface NewsService extends IService<News> {
    
    /**
     * 分页查询新闻列表
     * @param page 分页对象
     * @param title 标题关键词（可选）
     * @param category 分类（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    Page<News> pageNews(Page<News> page, String title, String category, Integer status);
    
    /**
     * 根据ID查询新闻详情（并增加浏览次数）
     * @param id 新闻ID
     * @return 新闻详情
     */
    News getNewsDetailById(Long id);
    
    /**
     * 发布新闻
     * @param news 新闻对象
     * @return 是否成功
     */
    boolean publishNews(News news);
    
    /**
     * 置顶/取消置顶
     * @param id 新闻ID
     * @param isTop 是否置顶（0-否 1-是）
     * @return 是否成功
     */
    boolean updateTopStatus(Long id, Integer isTop);
}
