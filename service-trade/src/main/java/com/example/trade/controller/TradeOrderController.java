package com.example.trade.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.trade.entity.TradeOrder;
import com.example.trade.feign.AssetFeignClient;
import com.example.trade.service.TradeOrderService;
import com.example.common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/trade/orders")
public class TradeOrderController {

    @Autowired
    private TradeOrderService tradeOrderService;
    
    @Autowired
    private AssetFeignClient assetFeignClient;

    // 获取市场挂单列表 (分页)
    @GetMapping("/market")
    public Result<Page<TradeOrder>> getMarketOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String direction) {
        
        Page<TradeOrder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TradeOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TradeOrder::getStatus, "OPEN"); // 只查询进行中的
        
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(TradeOrder::getItemType, type);
        }
        if (direction != null && !direction.isEmpty()) {
            queryWrapper.eq(TradeOrder::getDirection, direction);
        }
        
        queryWrapper.orderByDesc(TradeOrder::getCreateTime);
        
        return Result.success(tradeOrderService.page(pageParam, queryWrapper));
    }

    // 发布挂单
    @PostMapping("/publish")
    public Result<Boolean> publishOrder(@RequestBody TradeOrder order) {
        try {
            // 1. 如果是卖单，需要校验用户资产是否足够
            if ("SELL".equalsIgnoreCase(order.getDirection())) {
                log.info("校验卖单资产：userId={}, assetType={}, quantity={}", 
                         order.getUserId(), order.getItemType(), order.getQuantity());
                
                Result<Boolean> validateResult = assetFeignClient.validateAsset(
                    order.getUserId(), 
                    order.getItemType(), 
                    order.getQuantity()
                );
                
                if (validateResult == null || !Boolean.TRUE.equals(validateResult.getData())) {
                    String message = validateResult != null ? validateResult.getMessage() : "资产校验失败";
                    log.warn("资产校验不通过：{}", message);
                    return Result.error("资产不足，无法发布卖单：" + message);
                }
                
                log.info("资产校验通过");
            }
            
            // 2. 设置订单状态和时间
            order.setStatus("OPEN");
            order.setTradedQuantity(new java.math.BigDecimal(0));
            order.setCreateTime(java.time.LocalDateTime.now());
            order.setUpdateTime(java.time.LocalDateTime.now());
            
            // 3. 设置订单有效期（默认7天）
            if (order.getValidDays() == null || order.getValidDays() <= 0) {
                order.setValidDays(7);
            }
            order.setExpireTime(java.time.LocalDateTime.now().plusDays(order.getValidDays()));
            
            boolean success = tradeOrderService.save(order);
            
            if (success) {
                log.info("订单发布成功：orderId={}, expireTime={}", order.getId(), order.getExpireTime());
                return Result.success("订单发布成功", true);
            } else {
                return Result.error("订单发布失败");
            }
        } catch (Exception e) {
            log.error("发布订单失败", e);
            return Result.error("订单发布失败：" + e.getMessage());
        }
    }
    
    // 我的订单
    @GetMapping("/my")
    public Result<Page<TradeOrder>> getMyOrders(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Page<TradeOrder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TradeOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TradeOrder::getUserId, userId);
        queryWrapper.orderByDesc(TradeOrder::getCreateTime);
        return Result.success(tradeOrderService.page(pageParam, queryWrapper));
    }
    
    /**
     * 获取用户订单统计
     */
    @GetMapping("/stats/user")
    public Result<java.util.Map<String, Object>> getUserOrderStats(@RequestParam Long userId) {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        // 总订单数
        LambdaQueryWrapper<TradeOrder> totalQuery = new LambdaQueryWrapper<>();
        totalQuery.eq(TradeOrder::getUserId, userId);
        long totalCount = tradeOrderService.count(totalQuery);
        stats.put("totalOrders", totalCount);
        
        // 已成交订单数
        LambdaQueryWrapper<TradeOrder> closedQuery = new LambdaQueryWrapper<>();
        closedQuery.eq(TradeOrder::getUserId, userId);
        closedQuery.eq(TradeOrder::getStatus, "CLOSED");
        long closedCount = tradeOrderService.count(closedQuery);
        stats.put("closedOrders", closedCount);
        
        return Result.success("获取订单统计成功", stats);
    }
    
    /**
     * 获取今日交易金额统计
     */
    @GetMapping("/stats/today")
    public Result<java.math.BigDecimal> getTodayTradeAmount(@RequestParam Long userId) {
        java.time.LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
        
        LambdaQueryWrapper<TradeOrder> query = new LambdaQueryWrapper<>();
        query.eq(TradeOrder::getUserId, userId);
        query.eq(TradeOrder::getStatus, "CLOSED");
        query.ge(TradeOrder::getMatchTime, startOfDay);
        
        java.util.List<TradeOrder> orders = tradeOrderService.list(query);
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        
        for (TradeOrder order : orders) {
            if (order.getFinalPrice() != null && order.getTradedQuantity() != null) {
                java.math.BigDecimal amount = order.getFinalPrice().multiply(order.getTradedQuantity());
                total = total.add(amount);
            }
        }
        
        return Result.success("获取今日交易额成功", total);
    }    
    /**
     * 撤销订单
     */
    @PostMapping("/cancel/{orderId}")
    public Result<String> cancelOrder(@PathVariable Long orderId, @RequestParam Long userId) {
        try {
            TradeOrder order = tradeOrderService.getById(orderId);
            
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权撤销此订单");
            }
            
            if (!"OPEN".equals(order.getStatus())) {
                return Result.error("只能撤销挂单中的订单");
            }
            
            // 更新订单状态为已撤销
            order.setStatus("CANCELLED");
            order.setUpdateTime(java.time.LocalDateTime.now());
            boolean success = tradeOrderService.updateById(order);
            
            if (success) {
                log.info("订单撤销成功：orderId={}", orderId);
                
                // TODO: 如果是买单，需要解冻资金
                if ("BUY".equals(order.getDirection())) {
                    log.info("TODO: 解冻买单冻结资金，userId={}, amount={}", 
                             userId, order.getPrice().multiply(order.getQuantity()));
                }
                
                return Result.success("订单撤销成功");
            } else {
                return Result.error("订单撤销失败");
            }
        } catch (Exception e) {
            log.error("撤销订单失败", e);
            return Result.error("撤销订单失败：" + e.getMessage());
        }
    }}
