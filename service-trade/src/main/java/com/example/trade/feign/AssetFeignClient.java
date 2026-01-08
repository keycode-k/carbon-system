package com.example.trade.feign;

import com.example.common.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * 资产服务Feign客户端
 * 用于校验用户是否有足够的资产进行交易，以及交易后更新资产持仓
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
    
    /**
     * 更新碳配额数量（交易成交后调用）
     * @param userId 用户ID
     * @param year 年份
     * @param amount 变动数量（正数增加，负数减少）
     */
    @PostMapping("/api/assets/quota/update-quantity")
    Result<String> updateQuotaQuantity(@RequestParam("userId") Long userId,
                                       @RequestParam("year") Integer year,
                                       @RequestParam("amount") BigDecimal amount);
    
    /**
     * 更新碳信用数量（交易成交后调用）
     * @param userId 用户ID
     * @param projectId 项目ID
     * @param amount 变动数量（正数增加，负数减少）
     */
    @PostMapping("/api/assets/credit/update-quantity")
    Result<String> updateCreditQuantity(@RequestParam("userId") Long userId,
                                        @RequestParam("projectId") Long projectId,
                                        @RequestParam("amount") BigDecimal amount);
}
