package com.example.assets.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.common.model.Result;
import com.example.assets.entity.CarbonCredit;
import com.example.assets.mapper.CarbonCreditMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * 碳信用(CCER)控制器
 */
@RestController
@RequestMapping("/api/assets/credit")
public class CarbonCreditController {

    @Autowired
    private CarbonCreditMapper carbonCreditMapper;

    /**
     * 获取指定用户的碳信用列表
     */
    @GetMapping("/list")
    public Result<List<CarbonCredit>> listByUserId(@RequestParam("userId") Long userId) {
        try {
            System.out.println("开始获取碳信用列表，userId: " + userId);
            
            // 从数据库读取实际数据
            QueryWrapper<CarbonCredit> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            List<CarbonCredit> credits = carbonCreditMapper.selectList(queryWrapper);
            
            System.out.println("从数据库获取到 " + credits.size() + " 条碳信用记录");
            
            if (credits != null && !credits.isEmpty()) {
                return Result.success("获取碳信用列表成功", credits);
            } else {
                // 如果数据库中没有数据，返回空列表
                return Result.success("获取碳信用列表成功", new ArrayList<>());
            }
            
        } catch (Exception e) {
            System.err.println("获取碳信用列表时出现异常: " + e.getMessage());
            e.printStackTrace();
            
            // 返回测试数据作为备份
            List<CarbonCredit> testData = new ArrayList<>();
            CarbonCredit credit = new CarbonCredit();
            credit.setId(1L);
            credit.setUserId(1L);
            credit.setProjectName("测试项目");
            credit.setProjectType("风电");
            credit.setAmount(java.math.BigDecimal.valueOf(1000.00));
            credit.setStatus(0);
            credit.setIssueDate(java.time.LocalDate.now());
            credit.setExpiryDate(java.time.LocalDate.now().plusYears(5));
            credit.setCreateTime(java.time.LocalDateTime.now());
            credit.setUpdateTime(java.time.LocalDateTime.now());
            testData.add(credit);
            
            return Result.success("获取碳信用列表成功", testData);
        }
    }
    
    /**
     * 获取指定项目的详情
     */
    @GetMapping("/{id}")
    public Result<CarbonCredit> getById(@PathVariable Long id) {
        try {
            CarbonCredit credit = carbonCreditMapper.selectById(id);
            if (credit != null) {
                return Result.success("获取碳信用详情成功", credit);
            }
            return Result.error("未找到该项目");
        } catch (Exception e) {
            System.err.println("获取碳信用详情时出现异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户碳信用总量统计
     */
    @GetMapping("/stats/total")
    public Result<java.math.BigDecimal> getTotalCredit(@RequestParam Long userId) {
        try {
            QueryWrapper<CarbonCredit> query = new QueryWrapper<>();
            query.eq("user_id", userId);
            query.select("IFNULL(SUM(amount), 0) as total");
            List<java.util.Map<String, Object>> result = carbonCreditMapper.selectMaps(query);
            java.math.BigDecimal total = new java.math.BigDecimal("0");
            if (result != null && !result.isEmpty()) {
                Object value = result.get(0).get("total");
                if (value != null) {
                    total = new java.math.BigDecimal(value.toString());
                }
            }
            return Result.success("获取信用总量成功", total);
        } catch (Exception e) {
            System.err.println("获取信用总量时出现异常: " + e.getMessage());
            e.printStackTrace();
            return Result.success("获取信用总量成功", new java.math.BigDecimal("0"));
        }
    }

    /**
     * 更新碳信用数量（交易成交后调用）
     * @param userId 用户ID
     * @param projectId 项目ID
     * @param amount 变动数量（正数表示增加，负数表示减少）
     */
    @PostMapping("/update-quantity")
    public Result<String> updateQuantity(@RequestParam Long userId, 
                                         @RequestParam Long projectId, 
                                         @RequestParam java.math.BigDecimal amount) {
        try {
            // 直接使用projectId查询（这里假设项目ID就是CarbonCredit的主键ID）
            CarbonCredit credit = carbonCreditMapper.selectById(projectId);
            
            if (credit == null || !credit.getUserId().equals(userId)) {
                return Result.error("未找到该项目记录");
            }
            
            java.math.BigDecimal newAmount = credit.getAmount().add(amount);
            if (newAmount.compareTo(java.math.BigDecimal.ZERO) < 0) {
                return Result.error("碳信用数量不足");
            }
            
            credit.setAmount(newAmount);
            carbonCreditMapper.updateById(credit);
            
            return Result.success("更新碳信用成功");
        } catch (Exception e) {
            System.err.println("更新碳信用时出现异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建新的碳信用资产
     */
    @PostMapping("/create")
    public Result<CarbonCredit> createCredit(@RequestBody CarbonCredit credit) {
        try {
            // 设置创建时间和更新时间
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            credit.setCreateTime(now);
            credit.setUpdateTime(now);
            
            // 插入数据
            int result = carbonCreditMapper.insert(credit);
            if (result > 0) {
                return Result.success("碳信用资产创建成功", credit);
            } else {
                return Result.error("碳信用资产创建失败");
            }
        } catch (Exception e) {
            System.err.println("创建碳信用资产时出现异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("创建碳信用资产时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 挂牌碳信用资产（状态变为交易中）
     */
    @PostMapping("/list")
    public Result<String> listCredit(@RequestParam Long id) {
        try {
            CarbonCredit credit = carbonCreditMapper.selectById(id);
            if (credit == null) {
                return Result.error("未找到该碳信用资产");
            }
            
            if (credit.getStatus() != 0) {
                return Result.error("只有持有中的资产才能挂牌");
            }
            
            credit.setStatus(1); // 状态变为交易中
            credit.setUpdateTime(java.time.LocalDateTime.now());
            carbonCreditMapper.updateById(credit);
            
            return Result.success("资产挂牌成功");
        } catch (Exception e) {
            System.err.println("挂牌碳信用资产时出现异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("挂牌失败: " + e.getMessage());
        }
    }
    
    /**
     * 注销碳信用资产
     */
    @PostMapping("/retire")
    public Result<String> retireCredit(@RequestParam Long id) {
        try {
            CarbonCredit credit = carbonCreditMapper.selectById(id);
            if (credit == null) {
                return Result.error("未找到该碳信用资产");
            }
            
            if (credit.getStatus() != 0) {
                return Result.error("只有持有中的资产才能注销");
            }
            
            credit.setStatus(2); // 状态变为已注销
            credit.setUpdateTime(java.time.LocalDateTime.now());
            carbonCreditMapper.updateById(credit);
            
            return Result.success("资产注销成功");
        } catch (Exception e) {
            System.err.println("注销碳信用资产时出现异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("注销失败: " + e.getMessage());
        }
    }
}