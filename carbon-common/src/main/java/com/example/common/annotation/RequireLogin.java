package com.example.common.annotation;

import java.lang.annotation.*;

/**
 * 登录校验注解
 * 标注在方法或类上，表示需要登录才能访问
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireLogin {
    
    /**
     * 是否必须登录，默认为true
     */
    boolean required() default true;
}
