package com.example.development.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.development.entity.ProjectDocument;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目文档Mapper接口
 */
@Mapper
public interface ProjectDocumentMapper extends BaseMapper<ProjectDocument> {
}
