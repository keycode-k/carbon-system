package com.example.news.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.news.entity.News;
import com.example.news.mapper.NewsMapper;
import com.example.news.service.NewsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 新闻资讯服务实现类
 */
@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News> implements NewsService {
    
    @Override
    public Page<News> pageNews(Page<News> page, String title, String category, Integer status) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        
        // 标题模糊查询
        if (title != null && !title.trim().isEmpty()) {
            wrapper.like(News::getTitle, title);
        }
        
        // 分类筛选
        if (category != null && !category.trim().isEmpty()) {
            wrapper.eq(News::getCategory, category);
        }
        
        // 状态筛选（默认只查询已发布的）
        if (status != null) {
            wrapper.eq(News::getStatus, status);
        } else {
            wrapper.eq(News::getStatus, 1); // 默认只显示已发布
        }
        
        // 排序：置顶优先，然后按发布时间倒序
        wrapper.orderByDesc(News::getIsTop)
               .orderByDesc(News::getPublishTime);
        
        return this.page(page, wrapper);
    }
    
    @Override
    @Transactional
    public News getNewsDetailById(Long id) {
        News news = this.getById(id);
        if (news != null) {
            // 增加浏览次数
            baseMapper.increaseViewCount(id);
            // 重新查询以获取最新的浏览次数
            news = this.getById(id);
        }
        return news;
    }
    
    @Override
    @Transactional
    public boolean publishNews(News news) {
        if (news.getPublishTime() == null) {
            news.setPublishTime(LocalDateTime.now());
        }
        news.setStatus(1); // 设置为已发布
        
        if (news.getId() == null) {
            // 新增
            return this.save(news);
        } else {
            // 更新
            return this.updateById(news);
        }
    }
    
    @Override
    public boolean updateTopStatus(Long id, Integer isTop) {
        News news = new News();
        news.setId(id);
        news.setIsTop(isTop);
        return this.updateById(news);
    }
}
