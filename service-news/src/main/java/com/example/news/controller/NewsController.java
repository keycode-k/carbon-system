package com.example.news.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.model.Result;
import com.example.news.entity.News;
import com.example.news.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 新闻资讯控制器
 */
@RestController
@RequestMapping("/api/news")
public class NewsController {
    
    @Autowired
    private NewsService newsService;
    
    /**
     * 分页查询新闻列表
     * @param page 当前页码
     * @param size 每页大小
     * @param title 标题关键词（可选）
     * @param category 分类（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<Page<News>> list(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) Integer status) {
        
        Page<News> pageParam = new Page<>(page, size);
        Page<News> result = newsService.pageNews(pageParam, title, category, status);
        return Result.success(result);
    }
    
    /**
     * 查询新闻详情
     * @param id 新闻ID
     * @return 新闻详情
     */
    @GetMapping("/{id}")
    public Result<News> getDetail(@PathVariable Long id) {
        News news = newsService.getNewsDetailById(id);
        if (news == null) {
            return Result.error("新闻不存在");
        }
        return Result.success(news);
    }
    
    /**
     * 发布新闻（新增或更新）
     * @param news 新闻对象
     * @return 操作结果
     */
    @PostMapping
    public Result<String> publishNews(@RequestBody News news) {
        boolean success = newsService.publishNews(news);
        return success ? Result.success("发布成功") : Result.error("发布失败");
    }
    
    /**
     * 更新新闻
     * @param news 新闻对象
     * @return 操作结果
     */
    @PutMapping
    public Result<String> update(@RequestBody News news) {
        if (news.getId() == null) {
            return Result.error("新闻ID不能为空");
        }
        boolean success = newsService.updateById(news);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }
    
    /**
     * 删除新闻（逻辑删除）
     * @param id 新闻ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = newsService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
    
    /**
     * 置顶/取消置顶
     * @param id 新闻ID
     * @param isTop 是否置顶（0-否 1-是）
     * @return 操作结果
     */
    @PutMapping("/{id}/top")
    public Result<String> updateTopStatus(
            @PathVariable Long id,
            @RequestParam Integer isTop) {
        boolean success = newsService.updateTopStatus(id, isTop);
        return success ? Result.success("操作成功") : Result.error("操作失败");
    }
    
    /**
     * 修改新闻状态
     * @param id 新闻ID
     * @param status 状态（0-草稿 1-已发布 2-已下线）
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public Result<String> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        News news = new News();
        news.setId(id);
        news.setStatus(status);
        boolean success = newsService.updateById(news);
        return success ? Result.success("状态更新成功") : Result.error("状态更新失败");
    }
}
