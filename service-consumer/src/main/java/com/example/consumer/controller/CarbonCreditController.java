package com.example.consumer.controller;

import com.example.consumer.common.Result;
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
    public Result<List<CarbonCredit>> list(@RequestParam("token") String token) {
        // 1. Validate token and get user info
        Result<User> userResult = userFeignClient.getUserInfo(token);
        if (userResult.getCode() != 200 || userResult.getData() == null) {
            return Result.error(401, "Invalid token or expired");
        }
        
        Long userId = userResult.getData().getId();
        return carbonCreditFeignClient.listByUserId(userId);
    }
}
