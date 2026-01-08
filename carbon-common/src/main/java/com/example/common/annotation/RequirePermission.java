package com.example.common.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 标注在方法上，用于校验用户是否拥有指定权限
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    
    /**
     * 权限标识
     * 例如: "system:user:add", "trade:order:create"
     */
    String value();
    
    /**
     * 逻辑关系：AND 或 OR
     * 当有多个权限时生效
     */
    Logical logical() default Logical.AND;
    
    enum Logical {
        AND, OR
    }
}
