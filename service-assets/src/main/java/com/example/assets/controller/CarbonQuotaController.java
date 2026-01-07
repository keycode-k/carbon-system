package com.example.assets.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.assets.entity.CarbonQuota;
import com.example.assets.entity.CarbonQuotaDetail;
import com.example.assets.mapper.CarbonQuotaMapper;
import com.example.assets.mapper.CarbonQuotaDetailMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public CarbonQuota getQuota(@RequestParam Long userId, @RequestParam Integer year) {
        QueryWrapper<CarbonQuota> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        query.eq("year", year);
        return carbonQuotaMapper.selectOne(query);
    }
    
    /**
     * 获取用户所有年份的配额列表
     */
    @GetMapping("/list")
    public List<CarbonQuota> listQuotas(@RequestParam Long userId) {
        QueryWrapper<CarbonQuota> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        query.orderByDesc("year");
        return carbonQuotaMapper.selectList(query);
    }

    /**
     * 获取配额明细
     */
    @GetMapping("/detail/list")
    public List<CarbonQuotaDetail> listQuotaDetails(@RequestParam Long quotaId) {
        QueryWrapper<CarbonQuotaDetail> query = new QueryWrapper<>();
        query.eq("quota_id", quotaId);
        query.orderByDesc("change_date");
        return carbonQuotaDetailMapper.selectList(query);
    }

    /**
     * 新增或更新配额 (用于模拟数据)
     */
    @PostMapping("/save")
    public Map<String, Object> saveQuota(@RequestBody CarbonQuota quota) {
        Map<String, Object> result = new HashMap<>();
        if (quota.getId() == null) {
            carbonQuotaMapper.insert(quota);
        } else {
            carbonQuotaMapper.updateById(quota);
        }
        result.put("success", true);
        result.put("data", quota);
        return result;
    }
}
