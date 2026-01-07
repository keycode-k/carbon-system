package com.example.news.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.news.entity.News;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 新闻资讯Mapper接口
 */
@Mapper
public interface NewsMapper extends BaseMapper<News> {
    
    /**
     * 增加浏览次数
     * @param id 新闻ID
     * @return 影响行数
     */
    @Update("UPDATE news SET view_count = view_count + 1 WHERE id = #{id}")
    int increaseViewCount(Long id);
}
