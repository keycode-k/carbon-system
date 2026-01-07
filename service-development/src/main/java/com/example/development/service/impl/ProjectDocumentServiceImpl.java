package com.example.development.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.development.entity.ProjectDocument;
import com.example.development.mapper.ProjectDocumentMapper;
import com.example.development.service.ProjectDocumentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目文档服务实现类
 */
@Service
public class ProjectDocumentServiceImpl implements ProjectDocumentService {
    
    @Autowired
    private ProjectDocumentMapper documentMapper;
    
    @Override
    public boolean save(ProjectDocument document) {
        if (document.getCreateTime() == null) {
            document.setCreateTime(LocalDateTime.now());
        }
        document.setUpdateTime(LocalDateTime.now());
        return documentMapper.insert(document) > 0;
    }
    
    @Override
    public ProjectDocument getById(Long id) {
        return documentMapper.selectById(id);
    }
    
    @Override
    public Page<ProjectDocument> pageList(Long projectId, String docType, String status, int current, int size) {
        Page<ProjectDocument> page = new Page<>(current, size);
        LambdaQueryWrapper<ProjectDocument> wrapper = new LambdaQueryWrapper<>();
        
        if (projectId != null) {
            wrapper.eq(ProjectDocument::getProjectId, projectId);
        }
        if (StringUtils.hasText(docType)) {
            wrapper.eq(ProjectDocument::getDocType, docType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(ProjectDocument::getStatus, status);
        }
        
        wrapper.orderByDesc(ProjectDocument::getCreateTime);
        return documentMapper.selectPage(page, wrapper);
    }
    
    @Override
    public boolean updateById(ProjectDocument document) {
        document.setUpdateTime(LocalDateTime.now());
        return documentMapper.updateById(document) > 0;
    }
    
    @Override
    public boolean removeById(Long id) {
        return documentMapper.deleteById(id) > 0;
    }
    
    @Override
    public List<ProjectDocument> listByProjectId(Long projectId) {
        LambdaQueryWrapper<ProjectDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectDocument::getProjectId, projectId)
               .orderByDesc(ProjectDocument::getCreateTime);
        return documentMapper.selectList(wrapper);
    }
    
    @Override
    public ProjectDocument createNewVersion(Long documentId, String newVersion, Long uploadUserId) {
        // 查询原文档
        ProjectDocument originalDoc = documentMapper.selectById(documentId);
        if (originalDoc == null) {
            return null;
        }
        
        // 创建新版本文档
        ProjectDocument newDoc = new ProjectDocument();
        BeanUtils.copyProperties(originalDoc, newDoc);
        newDoc.setId(null); // 新记录
        newDoc.setVersion(newVersion);
        newDoc.setUploadUserId(uploadUserId);
        newDoc.setStatus("DRAFT"); // 新版本从草稿开始
        newDoc.setCreateTime(LocalDateTime.now());
        newDoc.setUpdateTime(LocalDateTime.now());
        
        // 插入新版本
        documentMapper.insert(newDoc);
        return newDoc;
    }
}
