package com.example.trade.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.model.Result;
import com.example.trade.entity.TradeRecord;
import com.example.trade.service.TradeRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 交易记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/trade/records")
public class TradeRecordController {

    @Autowired
    private TradeRecordService tradeRecordService;

    /**
     * 获取用户的交易记录（分页）
     */
    @GetMapping("/my")
    public Result<Page<TradeRecord>> getMyTradeRecords(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<TradeRecord> records = tradeRecordService.getUserTradeRecords(userId, current, size);
            return Result.success("获取交易记录成功", records);
        } catch (Exception e) {
            log.error("获取交易记录失败", e);
            return Result.error("获取交易记录失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有交易记录（分页，管理员使用）
     */
    @GetMapping("/all")
    public Result<Page<TradeRecord>> getAllTradeRecords(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<TradeRecord> records = tradeRecordService.getAllTradeRecords(current, size);
            return Result.success("获取所有交易记录成功", records);
        } catch (Exception e) {
            log.error("获取所有交易记录失败", e);
            return Result.error("获取所有交易记录失败：" + e.getMessage());
        }
    }

    /**
     * 根据订单ID查询交易记录
     */
    @GetMapping("/by-order/{orderId}")
    public Result<List<TradeRecord>> getByOrderId(@PathVariable Long orderId) {
        try {
            List<TradeRecord> records = tradeRecordService.getByOrderId(orderId);
            return Result.success("获取订单交易记录成功", records);
        } catch (Exception e) {
            log.error("获取订单交易记录失败", e);
            return Result.error("获取订单交易记录失败：" + e.getMessage());
        }
    }
}
