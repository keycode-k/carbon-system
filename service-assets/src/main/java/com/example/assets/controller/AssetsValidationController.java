package com.example.assets.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.assets.entity.CarbonCredit;
import com.example.assets.entity.CarbonQuota;
import com.example.assets.mapper.CarbonCreditMapper;
import com.example.assets.mapper.CarbonQuotaMapper;
import com.example.common.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * 资产校验Controller
 * 用于交易服务校验用户资产
 */
@RestController
@RequestMapping("/api/assets")
public class AssetsValidationController {
    
    @Autowired
    private CarbonQuotaMapper carbonQuotaMapper;
    
    @Autowired
    private CarbonCreditMapper carbonCreditMapper;
    
    /**
     * 校验用户资产是否足够
     * @param userId 用户ID
     * @param assetType 资产类型 QUOTA/CREDIT
     * @param quantity 需要的数量
     * @return 是否有足够资产
     */
    @GetMapping("/validate")
    public Result<Boolean> validateAsset(@RequestParam Long userId,
                                          @RequestParam String assetType,
                                          @RequestParam BigDecimal quantity) {
        try {
            if ("QUOTA".equalsIgnoreCase(assetType)) {
                // 校验配额
                return validateQuota(userId, quantity);
            } else if ("CREDIT".equalsIgnoreCase(assetType)) {
                // 校验信用
                return validateCredit(userId, quantity);
            } else {
                return Result.error("不支持的资产类型: " + assetType);
            }
        } catch (Exception e) {
            return Result.error("资产校验失败: " + e.getMessage());
        }
    }
    
    /**
     * 校验配额
     */
    private Result<Boolean> validateQuota(Long userId, BigDecimal quantity) {
        // 查询用户的最新年份配额（而不是只查当前年份）
        QueryWrapper<CarbonQuota> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        query.orderByDesc("year");  // 按年份降序排列
        query.last("LIMIT 1");      // 取最新一条
        
        CarbonQuota quota = carbonQuotaMapper.selectOne(query);
        
        if (quota == null) {
            return Result.success("用户没有配额数据", false);
        }
        
        // 计算可用配额 = 总配额 - 已核证排放量
        BigDecimal availableQuota = quota.getTotalQuota()
                .subtract(quota.getVerifiedEmission() != null ? quota.getVerifiedEmission() : BigDecimal.ZERO);
        
        if (availableQuota.compareTo(quantity) >= 0) {
            return Result.success("配额充足", true);
        } else {
            return Result.success("配额不足，可用: " + availableQuota + " tCO2e，需要: " + quantity + " tCO2e", false);
        }
    }
    
    /**
     * 校验信用
     */
    private Result<Boolean> validateCredit(Long userId, BigDecimal quantity) {
        // 查询用户的信用资产总量
        QueryWrapper<CarbonCredit> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        
        List<CarbonCredit> credits = carbonCreditMapper.selectList(query);
        
        if (credits == null || credits.isEmpty()) {
            return Result.success("用户没有信用资产", false);
        }
        
        // 计算总可用信用
        BigDecimal totalCredit = credits.stream()
                .map(CarbonCredit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalCredit.compareTo(quantity) >= 0) {
            return Result.success("信用充足", true);
        } else {
            return Result.success("信用不足，可用: " + totalCredit + "，需要: " + quantity, false);
        }
    }
}
