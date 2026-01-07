package com.example.consumer.feign;

import com.example.common.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 企业信息服务Feign客户端
 */
@FeignClient(name = "service-provider", contextId = "companyInfoFeignClient")
public interface CompanyInfoFeignClient {

    /**
     * 获取待审核企业数量
     */
    @GetMapping("/api/company/pending/count")
    Result<Long> getPendingCount();
}
