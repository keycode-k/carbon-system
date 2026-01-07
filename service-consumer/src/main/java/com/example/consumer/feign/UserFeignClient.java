package com.example.consumer.feign;

import com.example.common.model.Result;
import com.example.consumer.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "service-provider", contextId = "userFeignClient")
public interface UserFeignClient {

    @PostMapping("/api/user/register")
    Result<Object> register(@RequestBody User user);

    @PostMapping("/api/user/login")
    Result<Map<String, Object>> login(@RequestBody User user);

    @PostMapping("/api/user/logout")
    Result<Object> logout(@RequestParam("token") String token);
    
    @GetMapping("/api/user/info")
    Result<User> getUserInfo(@RequestParam("token") String token);

    @PostMapping("/api/user/update")
    Result<User> updateUser(@RequestBody User user);

    @PostMapping("/api/user/password")
    Result<Object> updatePassword(@RequestBody Map<String, String> params);

    @DeleteMapping("/api/user/delete")
    Result<Object> deleteUser(@RequestParam("userId") Long userId);
}
