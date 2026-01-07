package com.example.consumer.feign;

import com.example.common.model.Result;
import com.example.consumer.entity.CarbonCredit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "service-assets", contextId = "carbonCreditFeignClient")
public interface CarbonCreditFeignClient {

    @GetMapping("/api/assets/credit/list")
    Result<List<CarbonCredit>> listByUserId(@RequestParam("userId") Long userId);
    
    @GetMapping("/api/assets/credit/stats/total")
    Result<java.math.BigDecimal> getTotalCredit(@RequestParam("userId") Long userId);
}
