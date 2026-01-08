package com.example.trade.feign;

import com.example.common.model.Result;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 资产服务Feign客户端降级处理
 */
@Component
public class AssetFeignClientFallback implements AssetFeignClient {
    
    @Override
    public Result<Boolean> validateAsset(Long userId, String assetType, BigDecimal quantity) {
        // 降级处理：资产服务不可用时，返回false
        return Result.error("资产服务暂时不可用，请稍后重试");
    }
    
    @Override
    public Result<String> updateQuotaQuantity(Long userId, Integer year, BigDecimal amount) {
        // 降级处理
        return Result.error("资产服务暂时不可用，无法更新碳配额");
    }
    
    @Override
    public Result<String> updateCreditQuantity(Long userId, Long projectId, BigDecimal amount) {
        // 降级处理
        return Result.error("资产服务暂时不可用，无法更新碳信用");
    }
}
