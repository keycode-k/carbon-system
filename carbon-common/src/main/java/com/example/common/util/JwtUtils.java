package com.example.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT工具类
 * 提供Token生成、解析、验证等功能
 */
public class JwtUtils {
    
    /**
     * 默认密钥（生产环境应从配置文件读取）
     */
    private static final String DEFAULT_SECRET = "carbon-system-jwt-secret-key-2026-very-long-key-for-security";
    
    /**
     * 默认过期时间：2小时
     */
    private static final long DEFAULT_EXPIRATION = 2 * 60 * 60 * 1000L;
    
    /**
     * 刷新Token过期时间：7天
     */
    private static final long REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;
    
    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";
    
    /**
     * Header名称
     */
    public static final String HEADER_NAME = "Authorization";
    
    private final SecretKey secretKey;
    private final long expiration;
    
    public JwtUtils() {
        this(DEFAULT_SECRET, DEFAULT_EXPIRATION);
    }
    
    public JwtUtils(String secret) {
        this(secret, DEFAULT_EXPIRATION);
    }
    
    public JwtUtils(String secret, long expiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }
    
    /**
     * 生成访问Token
     */
    public String generateToken(Long userId, String username, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roles", roles);
        claims.put("type", "access");
        
        return createToken(claims, username, expiration);
    }
    
    /**
     * 生成刷新Token
     */
    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "refresh");
        
        return createToken(claims, username, REFRESH_EXPIRATION);
    }
    
    /**
     * 创建Token
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 解析Token
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token已过期", e);
        } catch (JwtException e) {
            throw new RuntimeException("Token无效", e);
        }
    }
    
    /**
     * 从Token获取用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }
    
    /**
     * 从Token获取用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }
    
    /**
     * 从Token获取角色列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Claims claims = parseToken(token);
        return (List<String>) claims.get("roles");
    }
    
    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
    
    /**
     * 检查Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * 判断是否为刷新Token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 从请求头获取Token
     */
    public static String getTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            return authHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
