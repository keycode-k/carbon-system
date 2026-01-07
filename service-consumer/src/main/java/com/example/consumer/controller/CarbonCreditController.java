package com.example.consumer.controller;

import com.example.common.model.Result;
import com.example.consumer.entity.CarbonCredit;
import com.example.consumer.feign.CarbonCreditFeignClient;
import com.example.consumer.feign.UserFeignClient;
import com.example.consumer.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets/credit")
public class CarbonCreditController {

    @Autowired
    private CarbonCreditFeignClient carbonCreditFeignClient;
    
    @Autowired
    private UserFeignClient userFeignClient;

    @GetMapping("/list")
    public Result<List<CarbonCredit>> list(@RequestParam(value = "userId", required = false, defaultValue = "1") Long userId,
                                           @RequestParam(value = "token", required = false) String token) {
        // 如果传了 token，验证并获取用户信息
        if (token != null && !token.isEmpty()) {
            Result<User> userResult = userFeignClient.getUserInfo(token);
            if (userResult.getCode() != 200 || userResult.getData() == null) {
                return Result.error(401, "Invalid token or expired");
            }
            userId = userResult.getData().getId();
        }
        
        // 直接使用 userId 查询（支持测试场景）
        return carbonCreditFeignClient.listByUserId(userId);
    }
}
