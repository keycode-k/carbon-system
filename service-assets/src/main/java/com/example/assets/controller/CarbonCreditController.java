package com.example.assets.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.assets.common.Result;
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
        return Result.success(list);
    }
    
    /**
     * 获取指定项目的详情
     */
    @GetMapping("/{id}")
    public Result<CarbonCredit> getById(@PathVariable Long id) {
        CarbonCredit credit = carbonCreditMapper.selectById(id);
        if (credit != null) {
            return Result.success(credit);
        }
        return Result.error("Project not found");
    }
}
