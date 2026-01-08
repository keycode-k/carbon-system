package com.example.provider.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统操作日志实体
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 操作用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 操作用户名
     */
    private String username;
    
    /**
     * 操作模块
     */
    private String module;
    
    /**
     * 操作类型（CREATE/UPDATE/DELETE/QUERY/LOGIN/LOGOUT/OTHER）
     */
    @TableField("operation_type")
    private String operationType;
    
    /**
     * 操作描述
     */
    private String description;
    
    /**
     * 请求方法
     */
    private String method;
    
    /**
     * 请求URL
     */
    @TableField("request_url")
    private String requestUrl;
    
    /**
     * 请求参数
     */
    @TableField(value = "request_params", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String requestParams;
    
    /**
     * 响应结果
     */
    @TableField(value = "response_result", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String responseResult;
    
    /**
     * 操作IP
     */
    private String ip;
    
    /**
     * 操作地点
     */
    @TableField(exist = false)
    private String location;
    
    /**
     * 浏览器
     */
    private String browser;
    
    /**
     * 操作系统
     */
    private String os;
    
    /**
     * 操作状态：0-失败 1-成功
     */
    private Integer status;
    
    /**
     * 错误信息
     */
    @TableField("error_msg")
    private String errorMsg;
    
    /**
     * 执行时长（毫秒）
     */
    private Long duration;
    
    /**
     * 操作时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
