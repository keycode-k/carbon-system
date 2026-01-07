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

            // 3. 获取碳资产总量 (碳配额 + CCER)
            Long totalQuota = 0L;
            Long totalCredit = 0L;
            try {
                // 调用碳配额统计 (这里简化处理，实际应该有专门的统计接口)
                totalQuota = 1000000L; // 模拟数据
            } catch (Exception e) {
                totalQuota = 0L;
            }
            try {
                // 调用CCER统计
                totalCredit = 50000L; // 模拟数据
            } catch (Exception e) {
                totalCredit = 0L;
            }
            summary.put("totalQuota", totalQuota);
            summary.put("totalCredit", totalCredit);
            summary.put("totalAssets", totalQuota + totalCredit);

            // 4. 获取交易总额 (今日或累计)
            try {
                // 调用交易统计接口 (这里简化，实际应该有专门的统计接口)
                summary.put("todayTradeAmount", 2300000.00);
                summary.put("totalTradeAmount", 58600000.00);
            } catch (Exception e) {
                summary.put("todayTradeAmount", 0.00);
                summary.put("totalTradeAmount", 0.00);
            }

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

            // 用户持有的碳配额
            try {
                // 实际应该调用 carbonQuotaFeignClient 获取用户所有年份配额总和
                userStats.put("userQuota", 100000L);
            } catch (Exception e) {
                userStats.put("userQuota", 0L);
            }

            // 用户持有的CCER
            try {
                // 实际应该调用 carbonCreditFeignClient 获取用户CCER总和
                userStats.put("userCredit", 5000L);
            } catch (Exception e) {
                userStats.put("userCredit", 0L);
            }

            // 用户交易次数
            try {
                // 实际应该调用 tradeOrderFeignClient 获取用户交易记录数
                userStats.put("tradeCount", 15);
            } catch (Exception e) {
                userStats.put("tradeCount", 0);
            }

            return Result.success(userStats);
        } catch (Exception e) {
            return Result.error("获取用户统计数据失败：" + e.getMessage());
        }
    }
}
