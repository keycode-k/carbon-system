package com.example.consumer.feign;

import com.example.common.model.Result;
import com.example.consumer.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 用户服务 Feign 客户端降级处理
 * 当服务调用失败时返回友好的错误信息
 */
@Component
public class UserFeignClientFallback implements FallbackFactory<UserFeignClient> {
    
    private static final Logger log = LoggerFactory.getLogger(UserFeignClientFallback.class);

    @Override
    public UserFeignClient create(Throwable cause) {
        log.error("用户服务调用失败，触发降级: {}", cause.getMessage());
        
        return new UserFeignClient() {
            @Override
            public Result<Object> register(User user) {
                return Result.error(503, "用户服务暂时不可用，请稍后重试");
            }

            @Override
            public Result<Map<String, Object>> login(User user) {
                return Result.error(503, "用户服务暂时不可用，请稍后重试");
            }

            @Override
            public Result<Object> logout(String authHeader) {
                return Result.error(503, "用户服务暂时不可用，请稍后重试");
            }

            @Override
            public Result<Map<String, Object>> getUserInfo(String authHeader) {
                log.warn("获取用户信息失败，返回降级响应");
                return Result.error(503, "用户服务暂时不可用，请稍后重试");
            }

            @Override
            public Result<User> updateUser(User user) {
                return Result.error(503, "用户服务暂时不可用，请稍后重试");
            }

            @Override
            public Result<Object> updatePassword(Map<String, String> params) {
                return Result.error(503, "用户服务暂时不可用，请稍后重试");
            }

            @Override
            public Result<Object> deleteUser(Long userId) {
                return Result.error(503, "用户服务暂时不可用，请稍后重试");
            }
        };
    }
}
