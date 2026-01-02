package com.example.provider.entity;

/**
 * 用户实体类
 */
public class User {

    /**
     * 账号
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    // 构造方法
    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // getter和setter方法
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
