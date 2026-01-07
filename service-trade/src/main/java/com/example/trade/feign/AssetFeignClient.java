package com.example.trade.feign;

import com.example.common.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * 资产服务Feign客户端
 * 用于校验用户是否有足够的资产进行交易
 */
@FeignClient(name = "service-assets", fallback = AssetFeignClientFallback.class)
public interface AssetFeignClient {
    
    /**
     * 校验用户资产
     * @param userId 用户ID
     * @param assetType 资产类型 QUOTA/CREDIT
     * @param quantity 数量
     * @return 是否有足够资产
     */
    @GetMapping("/api/assets/validate")
    Result<Boolean> validateAsset(@RequestParam("userId") Long userId, 
                                   @RequestParam("assetType") String assetType,
                                   @RequestParam("quantity") BigDecimal quantity);
}
