package com.example.consumer.controller;

import com.example.common.model.Result;
import com.example.consumer.feign.UserFeignClient;
import com.example.consumer.feign.CompanyInfoFeignClient;
import com.example.consumer.feign.TradeOrderFeignClient;
import com.example.consumer.feign.CarbonQuotaFeignClient;
import com.example.consumer.feign.CarbonCreditFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 仪表盘控制器 - 汇总各服务统计数据
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private CompanyInfoFeignClient companyInfoFeignClient;

    @Autowired
    private TradeOrderFeignClient tradeOrderFeignClient;

    @Autowired
    private CarbonQuotaFeignClient carbonQuotaFeignClient;

    @Autowired
    private CarbonCreditFeignClient carbonCreditFeignClient;

    /**
     * 获取仪表盘汇总数据
     * 返回：用户总数、企业认证数、碳资产总量、交易总额、待办事项数
     */
    @GetMapping("/summary")
    public Result<Map<String, Object>> getSummary(@RequestParam(required = false, defaultValue = "1") Long userId) {
        try {
            Map<String, Object> summary = new HashMap<>();

            // 1. 获取用户总数 (模拟：后续可通过 UserFeignClient 调用统计接口)
            summary.put("totalUsers", 128);

            // 2. 获取企业认证数 (调用待审核企业数量接口)
            try {
                Result<Long> pendingResult = companyInfoFeignClient.getPendingCount();
                summary.put("certifiedCompanies", pendingResult != null && pendingResult.getData() != null ? pendingResult.getData() : 0L);
            } catch (Exception e) {
                summary.put("certifiedCompanies", 0L);
            }

            // 3. 获取碳资产总量 (碳配额 + CCER) - 使用真实数据
            java.math.BigDecimal totalQuota = java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalCredit = java.math.BigDecimal.ZERO;
            
            try {
                Result<java.math.BigDecimal> quotaResult = carbonQuotaFeignClient.getTotalQuota(userId);
                if (quotaResult != null && quotaResult.getData() != null) {
                    totalQuota = quotaResult.getData();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            try {
                Result<java.math.BigDecimal> creditResult = carbonCreditFeignClient.getTotalCredit(userId);
                if (creditResult != null && creditResult.getData() != null) {
                    totalCredit = creditResult.getData();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            summary.put("totalQuota", totalQuota);
            summary.put("totalCredit", totalCredit);
            summary.put("totalAssets", totalQuota.add(totalCredit));

            // 4. 获取交易总额 (今日) - 使用真实数据
            try {
                Result<java.math.BigDecimal> todayResult = tradeOrderFeignClient.getTodayTradeAmount(userId);
                if (todayResult != null && todayResult.getData() != null) {
                    summary.put("todayTradeAmount", todayResult.getData());
                } else {
                    summary.put("todayTradeAmount", java.math.BigDecimal.ZERO);
                }
            } catch (Exception e) {
                e.printStackTrace();
                summary.put("todayTradeAmount", java.math.BigDecimal.ZERO);
            }
            
            summary.put("totalTradeAmount", 0.00); // 累计交易额暂时保留，后续可扩展

            // 5. 获取待办事项数 (待审批企业数)
            try {
                Result<Long> pendingResult = companyInfoFeignClient.getPendingCount();
                summary.put("pendingTasks", pendingResult != null && pendingResult.getData() != null ? pendingResult.getData() : 0L);
            } catch (Exception e) {
                summary.put("pendingTasks", 0L);
            }

            // 6. 系统运行状态
            summary.put("systemStatus", "正常运行");
            summary.put("lastUpdateTime", System.currentTimeMillis());

            return Result.success(summary);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取仪表盘数据失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户个人统计数据
     */
    @GetMapping("/user-stats")
    public Result<Map<String, Object>> getUserStats(@RequestParam Long userId) {
        try {
            Map<String, Object> userStats = new HashMap<>();

            // 用户持有的碳配额 - 使用真实数据
            try {
                Result<java.math.BigDecimal> quotaResult = carbonQuotaFeignClient.getTotalQuota(userId);
                if (quotaResult != null && quotaResult.getData() != null) {
                    userStats.put("userQuota", quotaResult.getData());
                } else {
                    userStats.put("userQuota", java.math.BigDecimal.ZERO);
                }
            } catch (Exception e) {
                e.printStackTrace();
                userStats.put("userQuota", java.math.BigDecimal.ZERO);
            }

            // 用户持有的CCER - 使用真实数据
            try {
                Result<java.math.BigDecimal> creditResult = carbonCreditFeignClient.getTotalCredit(userId);
                if (creditResult != null && creditResult.getData() != null) {
                    userStats.put("userCredit", creditResult.getData());
                } else {
                    userStats.put("userCredit", java.math.BigDecimal.ZERO);
                }
            } catch (Exception e) {
                e.printStackTrace();
                userStats.put("userCredit", java.math.BigDecimal.ZERO);
            }

            // 用户交易次数 - 使用真实数据
            try {
                Result<Map<String, Object>> statsResult = tradeOrderFeignClient.getUserOrderStats(userId);
                if (statsResult != null && statsResult.getData() != null) {
                    Map<String, Object> stats = statsResult.getData();
                    userStats.put("tradeCount", stats.get("closedOrders"));
                } else {
                    userStats.put("tradeCount", 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
                userStats.put("tradeCount", 0);
            }

            return Result.success(userStats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取用户统计数据失败：" + e.getMessage());
        }
    }
}
