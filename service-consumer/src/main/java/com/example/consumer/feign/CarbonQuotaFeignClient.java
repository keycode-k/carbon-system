package com.example.consumer.feign;

import com.example.consumer.entity.CarbonQuota;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "service-provider", contextId = "carbonQuotaFeignClient")
public interface CarbonQuotaFeignClient {

    @GetMapping("/api/assets/quota/get")
    CarbonQuota getQuota(@RequestParam("userId") Long userId, @RequestParam("year") Integer year);

    @GetMapping("/api/assets/quota/list")
    List<CarbonQuota> listQuotas(@RequestParam("userId") Long userId);
}
