package com.example.provider.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码加密工具类
 * 使用BCrypt算法进行密码加密和验证
 */
@Component
public class PasswordUtils {
    
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    /**
     * 加密密码
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }
    
    /**
     * 验证密码
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        // 兼容旧的明文密码
        if (encodedPassword != null && !encodedPassword.startsWith("$2a$")) {
            return rawPassword.equals(encodedPassword);
        }
        return encoder.matches(rawPassword, encodedPassword);
    }
    
    /**
     * 判断密码是否已加密
     * @param password 密码
     * @return 是否已加密
     */
    public static boolean isEncoded(String password) {
        return password != null && password.startsWith("$2a$");
    }
}
