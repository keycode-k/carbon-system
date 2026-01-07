package com.example.development.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.model.Result;
import com.example.development.entity.ProjectDocument;
import com.example.development.service.ProjectDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 项目文档管理Controller
 */
@RestController
@RequestMapping("/api/development/document")
public class ProjectDocumentController {
    
    @Autowired
    private ProjectDocumentService documentService;
    
    // 文件上传目录（实际应从配置文件读取）
    private static final String UPLOAD_DIR = "uploads/documents/";
    
    /**
     * 上传文档
     */
    @PostMapping("/upload")
    public Result<ProjectDocument> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("projectId") Long projectId,
            @RequestParam("docName") String docName,
            @RequestParam(value = "docType", required = false) String docType,
            @RequestParam("uploadUserId") Long uploadUserId) {
        
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        
        try {
            // 确保上传目录存在
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : "";
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = Paths.get(UPLOAD_DIR + filename);
            Files.write(filePath, file.getBytes());
            
            // 创建文档记录
            ProjectDocument document = new ProjectDocument();
            document.setProjectId(projectId);
            document.setDocName(docName);
            document.setDocType(docType);
            document.setFileUrl(UPLOAD_DIR + filename);
            document.setFileSize(file.getSize());
            document.setVersion("v1.0");
            document.setUploadUserId(uploadUserId);
            document.setStatus("DRAFT");
            document.setCreateTime(LocalDateTime.now());
            document.setUpdateTime(LocalDateTime.now());
            
            documentService.save(document);
            
            return Result.success(document);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 文档列表（分页）
     */
    @GetMapping("/list")
    public Result<Page<ProjectDocument>> getDocumentList(
            @RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam(value = "docType", required = false) String docType,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<ProjectDocument> result = documentService.pageList(projectId, docType, status, page, size);
        return Result.success(result);
    }
    
    /**
     * 文档详情
     */
    @GetMapping("/{id}")
    public Result<ProjectDocument> getDocumentById(@PathVariable Long id) {
        ProjectDocument document = documentService.getById(id);
        if (document == null) {
            return Result.error("文档不存在");
        }
        return Result.success(document);
    }
    
    /**
     * 下载文档
     * 注：实际应返回文件流，这里简化为返回文件URL
     */
    @GetMapping("/download/{id}")
    public Result<String> downloadDocument(@PathVariable Long id) {
        ProjectDocument document = documentService.getById(id);
        if (document == null) {
            return Result.error("文档不存在");
        }
        return Result.success(document.getFileUrl());
    }
    
    /**
     * 更新文档信息
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateDocument(
            @PathVariable Long id,
            @RequestBody ProjectDocument document) {
        
        document.setId(id);
        boolean success = documentService.updateById(document);
        if (success) {
            return Result.success(true);
        }
        return Result.error("更新失败");
    }
    
    /**
     * 删除文档
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteDocument(@PathVariable Long id) {
        boolean success = documentService.removeById(id);
        if (success) {
            return Result.success(true);
        }
        return Result.error("删除失败");
    }
    
    /**
     * 创建新版本
     */
    @PostMapping("/{id}/version")
    public Result<ProjectDocument> createVersion(
            @PathVariable Long id,
            @RequestParam("version") String newVersion,
            @RequestParam("uploadUserId") Long uploadUserId) {
        
        ProjectDocument newDoc = documentService.createNewVersion(id, newVersion, uploadUserId);
        if (newDoc != null) {
            return Result.success(newDoc);
        }
        return Result.error("创建版本失败");
    }
    
    /**
     * 按项目查询文档
     */
    @GetMapping("/project/{projectId}")
    public Result<List<ProjectDocument>> getDocumentsByProject(@PathVariable Long projectId) {
        List<ProjectDocument> documents = documentService.listByProjectId(projectId);
        return Result.success(documents);
    }
}
