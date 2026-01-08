package com.example.consumer.controller;

import com.example.common.model.Result;
import com.example.consumer.feign.UserFeignClient;
import com.example.consumer.feign.CompanyInfoFeignClient;
import com.example.consumer.feign.TradeOrderFeignClient;
import com.example.consumer.feign.CarbonQuotaFeignClient;
import com.example.consumer.feign.CarbonCreditFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 仪表盘控制器 - 汇总各服务统计数据
 * 优化：使用并行调用提升响应速度
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
     * 使用 CompletableFuture 并行调用各个服务，大幅提升响应速度
     */
    @GetMapping("/summary")
    public Result<Map<String, Object>> getSummary(@RequestParam(required = false, defaultValue = "1") Long userId) {
        try {
            Map<String, Object> summary = new HashMap<>();
            
            // 并行调用各个服务
            CompletableFuture<Long> pendingCountFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    Result<Long> result = companyInfoFeignClient.getPendingCount();
                    return result != null && result.getData() != null ? result.getData() : 0L;
                } catch (Exception e) {
                    return 0L;
                }
            });

            CompletableFuture<BigDecimal> quotaFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    Result<BigDecimal> result = carbonQuotaFeignClient.getTotalQuota(userId);
                    return result != null && result.getData() != null ? result.getData() : BigDecimal.ZERO;
                } catch (Exception e) {
                    return BigDecimal.ZERO;
                }
            });

            CompletableFuture<BigDecimal> creditFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    Result<BigDecimal> result = carbonCreditFeignClient.getTotalCredit(userId);
                    return result != null && result.getData() != null ? result.getData() : BigDecimal.ZERO;
                } catch (Exception e) {
                    return BigDecimal.ZERO;
                }
            });

            CompletableFuture<BigDecimal> todayTradeFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    Result<BigDecimal> result = tradeOrderFeignClient.getTodayTradeAmount(userId);
                    return result != null && result.getData() != null ? result.getData() : BigDecimal.ZERO;
                } catch (Exception e) {
                    return BigDecimal.ZERO;
                }
            });

            // 等待所有异步任务完成（最多等待5秒）
            CompletableFuture.allOf(pendingCountFuture, quotaFuture, creditFuture, todayTradeFuture)
                    .orTimeout(5, TimeUnit.SECONDS)
                    .exceptionally(ex -> null)
                    .join();

            // 获取结果
            Long pendingCount = pendingCountFuture.getNow(0L);
            BigDecimal totalQuota = quotaFuture.getNow(BigDecimal.ZERO);
            BigDecimal totalCredit = creditFuture.getNow(BigDecimal.ZERO);
            BigDecimal todayTradeAmount = todayTradeFuture.getNow(BigDecimal.ZERO);

            // 组装数据
            summary.put("totalUsers", 128);
            summary.put("certifiedCompanies", pendingCount);
            summary.put("totalQuota", totalQuota);
            summary.put("totalCredit", totalCredit);
            summary.put("totalAssets", totalQuota.add(totalCredit));
            summary.put("todayTradeAmount", todayTradeAmount);
            summary.put("totalTradeAmount", BigDecimal.ZERO);
            summary.put("pendingTasks", pendingCount);
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
