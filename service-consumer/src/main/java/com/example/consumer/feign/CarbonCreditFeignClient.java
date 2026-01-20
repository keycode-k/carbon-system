package com.example.consumer.feign;

import com.example.common.model.Result;
import com.example.consumer.config.FeignConfig;
import com.example.consumer.entity.CarbonCredit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "service-assets", contextId = "carbonCreditFeignClient", configuration = FeignConfig.class)
public interface CarbonCreditFeignClient {

    @GetMapping("/api/assets/credit/list")
    Result<List<CarbonCredit>> listByUserId(@RequestParam("userId") Long userId);
    
    @GetMapping("/api/assets/credit/stats/total")
    Result<java.math.BigDecimal> getTotalCredit(@RequestParam("userId") Long userId);
    
    @PostMapping("/api/assets/credit/create")
    Result<CarbonCredit> createCredit(@RequestBody CarbonCredit credit);
}
