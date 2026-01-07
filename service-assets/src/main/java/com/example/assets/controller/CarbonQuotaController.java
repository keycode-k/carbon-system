package com.example.assets.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.assets.entity.CarbonQuota;
import com.example.assets.entity.CarbonQuotaDetail;
import com.example.assets.mapper.CarbonQuotaMapper;
import com.example.assets.mapper.CarbonQuotaDetailMapper;
import com.example.common.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 碳配额管理 - 服务端接口
 */
@RestController
@RequestMapping("/api/assets/quota")
public class CarbonQuotaController {

    @Autowired
    private CarbonQuotaMapper carbonQuotaMapper;

    @Autowired
    private CarbonQuotaDetailMapper carbonQuotaDetailMapper;

    /**
     * 获取指定年份的配额信息
     */
    @GetMapping("/get")
    public Result<CarbonQuota> getQuota(@RequestParam Long userId, @RequestParam Integer year) {
        QueryWrapper<CarbonQuota> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        query.eq("year", year);
        CarbonQuota quota = carbonQuotaMapper.selectOne(query);
        return Result.success("获取配额信息成功", quota);
    }
    
    /**
     * 获取用户所有年份的配额列表
     */
    @GetMapping("/list")
    public Result<List<CarbonQuota>> listQuotas(@RequestParam Long userId) {
        QueryWrapper<CarbonQuota> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        query.orderByDesc("year");
        List<CarbonQuota> list = carbonQuotaMapper.selectList(query);
        return Result.success("获取配额列表成功", list);
    }

    /**
     * 获取配额明细
     */
    @GetMapping("/detail/list")
    public Result<List<CarbonQuotaDetail>> listQuotaDetails(@RequestParam Long quotaId) {
        QueryWrapper<CarbonQuotaDetail> query = new QueryWrapper<>();
        query.eq("quota_id", quotaId);
        query.orderByDesc("change_date");
        List<CarbonQuotaDetail> details = carbonQuotaDetailMapper.selectList(query);
        return Result.success("获取配额明细成功", details);
    }

    /**
     * 新增或更新配额 (用于模拟数据)
     */
    @PostMapping("/save")
    public Result<CarbonQuota> saveQuota(@RequestBody CarbonQuota quota) {
        if (quota.getId() == null) {
            carbonQuotaMapper.insert(quota);
        } else {
            carbonQuotaMapper.updateById(quota);
        }
        return Result.success("保存配额成功", quota);
    }
    
    /**
     * 获取用户配额总量统计
     */
    @GetMapping("/stats/total")
    public Result<java.math.BigDecimal> getTotalQuota(@RequestParam Long userId) {
        QueryWrapper<CarbonQuota> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        query.select("IFNULL(SUM(total_quota), 0) as total");
        List<java.util.Map<String, Object>> result = carbonQuotaMapper.selectMaps(query);
        java.math.BigDecimal total = new java.math.BigDecimal("0");
        if (result != null && !result.isEmpty()) {
            Object value = result.get(0).get("total");
            if (value != null) {
                total = new java.math.BigDecimal(value.toString());
            }
        }
        return Result.success("获取配额总量成功", total);
    }

    /**
     * 更新配额数量（交易成交后调用）
     * @param userId 用户ID
     * @param year 年份
     * @param amount 变动数量（正数表示增加，负数表示减少）
     */
    @PostMapping("/update-quantity")
    public Result<String> updateQuantity(@RequestParam Long userId, 
                                         @RequestParam Integer year, 
                                         @RequestParam java.math.BigDecimal amount) {
        QueryWrapper<CarbonQuota> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        query.eq("year", year);
        CarbonQuota quota = carbonQuotaMapper.selectOne(query);
        
        if (quota == null) {
            return Result.error("未找到该年份的配额记录");
        }
        
        java.math.BigDecimal newTotal = quota.getTotalQuota().add(amount);
        if (newTotal.compareTo(java.math.BigDecimal.ZERO) < 0) {
            return Result.error("配额数量不足");
        }
        
        quota.setTotalQuota(newTotal);
        carbonQuotaMapper.updateById(quota);
        
        return Result.success("更新配额成功");
    }
}
