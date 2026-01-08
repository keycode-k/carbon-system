package com.example.common.annotation;

import java.lang.annotation.*;

/**
 * 角色校验注解
 * 标注在方法上，用于校验用户是否拥有指定角色
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    
    /**
     * 角色编码
     * 例如: "ADMIN", "USER", "ENTERPRISE"
     */
    String[] value();
    
    /**
     * 逻辑关系：AND 或 OR
     * 当有多个角色时生效
     */
    RequirePermission.Logical logical() default RequirePermission.Logical.OR;
}
