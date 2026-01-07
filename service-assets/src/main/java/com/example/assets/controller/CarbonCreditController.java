package com.example.assets.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.common.model.Result;
import com.example.assets.entity.CarbonCredit;
import com.example.assets.mapper.CarbonCreditMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        QueryWrapper<CarbonCredit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("issue_date");
        List<CarbonCredit> list = carbonCreditMapper.selectList(queryWrapper);
        return Result.success("获取碳信用列表成功", list);
    }
    
    /**
     * 获取指定项目的详情
     */
    @GetMapping("/{id}")
    public Result<CarbonCredit> getById(@PathVariable Long id) {
        CarbonCredit credit = carbonCreditMapper.selectById(id);
        if (credit != null) {
            return Result.success("获取碳信用详情成功", credit);
        }
        return Result.error("未找到该项目");
    }
    
    /**
     * 获取用户碳信用总量统计
     */
    @GetMapping("/stats/total")
    public Result<java.math.BigDecimal> getTotalCredit(@RequestParam Long userId) {
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
    }
}
