package com.example.consumer.feign;

import com.example.common.model.Result;
import com.example.consumer.config.FeignConfig;
import com.example.consumer.entity.CarbonQuota;
import com.example.consumer.entity.CarbonQuotaDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "service-assets", contextId = "carbonQuotaFeignClient", configuration = FeignConfig.class)
public interface CarbonQuotaFeignClient {

    @GetMapping("/api/assets/quota/get")
    Result<CarbonQuota> getQuota(@RequestParam("userId") Long userId, @RequestParam("year") Integer year);

    @GetMapping("/api/assets/quota/list")
    Result<List<CarbonQuota>> listQuotas(@RequestParam("userId") Long userId);

    @GetMapping("/api/assets/quota/detail/list")
    Result<List<CarbonQuotaDetail>> listQuotaDetails(@RequestParam("quotaId") Long quotaId);
    
    @GetMapping("/api/assets/quota/stats/total")
    Result<java.math.BigDecimal> getTotalQuota(@RequestParam("userId") Long userId);
}
