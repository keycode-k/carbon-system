package com.example.development.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.development.entity.ProjectDocument;

import java.util.List;

/**
 * 项目文档服务接口
 */
public interface ProjectDocumentService {
    
    /**
     * 保存文档信息
     */
    boolean save(ProjectDocument document);
    
    /**
     * 根据ID查询文档
     */
    ProjectDocument getById(Long id);
    
    /**
     * 分页查询文档列表
     */
    Page<ProjectDocument> pageList(Long projectId, String docType, String status, int current, int size);
    
    /**
     * 更新文档信息
     */
    boolean updateById(ProjectDocument document);
    
    /**
     * 删除文档
     */
    boolean removeById(Long id);
    
    /**
     * 根据项目ID查询文档列表
     */
    List<ProjectDocument> listByProjectId(Long projectId);
    
    /**
     * 创建新版本
     */
    ProjectDocument createNewVersion(Long documentId, String newVersion, Long uploadUserId);
}
