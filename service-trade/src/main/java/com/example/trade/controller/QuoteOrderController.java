package com.example.trade.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.model.Result;
import com.example.trade.entity.QuoteOrder;
import com.example.trade.service.QuoteOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 询报价单控制器
 */
@RestController
@RequestMapping("/api/trade/quotes")
public class QuoteOrderController {
    
    @Autowired
    private QuoteOrderService quoteOrderService;
    
    /**
     * 分页查询市场询报价单
     */
    @GetMapping("/market")
    public Result<Page<QuoteOrder>> getMarketQuotes(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String assetType,
            @RequestParam(required = false) String status) {
        
        Page<QuoteOrder> result = quoteOrderService.pageQuery(page, size, type, assetType, status);
        return Result.success(result);
    }
    
    /**
     * 查询我的询报价单
     */
    @GetMapping("/my")
    public Result<Page<QuoteOrder>> getMyQuotes(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Page<QuoteOrder> result = quoteOrderService.getMyQuotes(userId, page, size);
        return Result.success(result);
    }
    
    /**
     * 查询询价单详情
     */
    @GetMapping("/{id}")
    public Result<QuoteOrder> getQuoteDetail(@PathVariable Long id) {
        QuoteOrder quoteOrder = quoteOrderService.getById(id);
        if (quoteOrder == null) {
            return Result.error("询价单不存在");
        }
        return Result.success(quoteOrder);
    }
    
    /**
     * 发布询价单
     */
    @PostMapping("/publish")
    public Result<String> publishQuote(@RequestBody QuoteOrder quoteOrder) {
        try {
            boolean success = quoteOrderService.publishQuote(quoteOrder);
            return success ? Result.success("发布成功") : Result.error("发布失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 报价（商家对询价单进行报价）
     */
    @PostMapping("/{id}/quote")
    public Result<String> makeQuote(
            @PathVariable Long id,
            @RequestParam BigDecimal finalPrice) {
        try {
            boolean success = quoteOrderService.makeQuote(id, finalPrice);
            return success ? Result.success("报价成功") : Result.error("报价失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 接受报价
     */
    @PostMapping("/{id}/accept")
    public Result<String> acceptQuote(
            @PathVariable Long id,
            @RequestParam Long userId) {
        try {
            boolean success = quoteOrderService.acceptQuote(id, userId);
            return success ? Result.success("接受成功") : Result.error("接受失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 拒绝报价
     */
    @PostMapping("/{id}/reject")
    public Result<String> rejectQuote(
            @PathVariable Long id,
            @RequestParam Long userId) {
        try {
            boolean success = quoteOrderService.rejectQuote(id, userId);
            return success ? Result.success("拒绝成功") : Result.error("拒绝失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 取消询价单
     */
    @PostMapping("/{id}/cancel")
    public Result<String> cancelQuote(
            @PathVariable Long id,
            @RequestParam Long userId) {
        try {
            boolean success = quoteOrderService.cancelQuote(id, userId);
            return success ? Result.success("取消成功") : Result.error("取消失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
