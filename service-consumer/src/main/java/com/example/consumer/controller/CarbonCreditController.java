package com.example.consumer.controller;

import com.example.common.model.Result;
import com.example.consumer.entity.CarbonCredit;
import com.example.consumer.feign.CarbonCreditFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets/credit")
public class CarbonCreditController {

    @Autowired
    private CarbonCreditFeignClient carbonCreditFeignClient;

    @GetMapping("/list")
    public Result<List<CarbonCredit>> list(@RequestParam(value = "userId", required = false, defaultValue = "1") Long userId) {
        // 直接使用 userId 查询
        return carbonCreditFeignClient.listByUserId(userId);
    }
    
    @PostMapping("/create")
    public Result<CarbonCredit> createCredit(@RequestBody CarbonCredit credit) {
        return carbonCreditFeignClient.createCredit(credit);
    }
}
