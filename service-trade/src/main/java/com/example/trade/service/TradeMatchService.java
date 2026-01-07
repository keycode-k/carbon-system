package com.example.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.trade.entity.TradeOrder;
import com.example.trade.entity.TradeRecord;
import com.example.trade.mapper.TradeOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易撮合服务
 * 实现价格优先、时间优先的撮合算法
 */
@Slf4j
@Service
public class TradeMatchService {
    
    @Autowired
    private TradeOrderMapper tradeOrderMapper;
    
    @Autowired
    private TradeAccountService tradeAccountService;
    
    @Autowired
    private TradeRecordService tradeRecordService;
    
    @Autowired(required = false)
    private RestTemplate restTemplate;
    
    /**
     * 执行自动撮合
     * 匹配规则：
     * 1. 买单价格 >= 卖单价格
     * 2. 价格优先：买单从高到低，卖单从低到高
     * 3. 时间优先：同价格按时间排序
     */
    @Transactional(rollbackFor = Exception.class)
    public void matchOrders() {
        log.info("开始执行交易撮合...");
        
        // 1. 先关闭过期订单
        closeExpiredOrders();
        
        // 2. 遍历所有资产类型进行撮合
        String[] assetTypes = {"QUOTA", "CREDIT"};
        
        for (String assetType : assetTypes) {
            matchOrdersByAssetType(assetType);
        }
        
        log.info("交易撮合执行完毕");
    }
    
    /**
     * 关闭过期订单
     */
    private void closeExpiredOrders() {
        LambdaQueryWrapper<TradeOrder> query = new LambdaQueryWrapper<>();
        query.eq(TradeOrder::getStatus, "OPEN");
        query.le(TradeOrder::getExpireTime, LocalDateTime.now());
        
        List<TradeOrder> expiredOrders = tradeOrderMapper.selectList(query);
        
        if (expiredOrders != null && !expiredOrders.isEmpty()) {
            for (TradeOrder order : expiredOrders) {
                order.setStatus("CANCELLED");
                order.setUpdateTime(LocalDateTime.now());
                tradeOrderMapper.updateById(order);
                
                log.info("订单已过期自动关闭：orderId={}, expireTime={}", order.getId(), order.getExpireTime());
                
                // TODO: 如果是买单，需要解冻资金
                if ("BUY".equals(order.getDirection())) {
                    try {
                        BigDecimal frozenAmount = order.getPrice().multiply(
                            order.getQuantity().subtract(order.getTradedQuantity())
                        );
                        boolean unfrozen = tradeAccountService.unfreezeAmount(order.getUserId(), frozenAmount);
                        if (unfrozen) {
                            log.info("过期订单已解冻资金：userId={}, amount={}", order.getUserId(), frozenAmount);
                        }
                    } catch (Exception e) {
                        log.error("解冻过期订单资金失败", e);
                    }
                }
            }
            log.info("已关闭{}个过期订单", expiredOrders.size());
        }
    }
    
    /**
     * 按资产类型撮合订单
     */
    private void matchOrdersByAssetType(String assetType) {
        // 1. 查询所有未完成的买单（按价格从高到低，时间从早到晚）
        LambdaQueryWrapper<TradeOrder> buyQuery = new LambdaQueryWrapper<>();
        buyQuery.eq(TradeOrder::getItemType, assetType)
                .eq(TradeOrder::getDirection, "BUY")
                .eq(TradeOrder::getStatus, "OPEN")
                .orderByDesc(TradeOrder::getPrice)  // 价格从高到低
                .orderByAsc(TradeOrder::getCreateTime); // 时间优先
        
        List<TradeOrder> buyOrders = tradeOrderMapper.selectList(buyQuery);
        
        // 2. 查询所有未完成的卖单（按价格从低到高，时间从早到晚）
        LambdaQueryWrapper<TradeOrder> sellQuery = new LambdaQueryWrapper<>();
        sellQuery.eq(TradeOrder::getItemType, assetType)
                 .eq(TradeOrder::getDirection, "SELL")
                 .eq(TradeOrder::getStatus, "OPEN")
                 .orderByAsc(TradeOrder::getPrice)  // 价格从低到高
                 .orderByAsc(TradeOrder::getCreateTime); // 时间优先
        
        List<TradeOrder> sellOrders = tradeOrderMapper.selectList(sellQuery);
        
        // 3. 执行撮合
        for (TradeOrder buyOrder : buyOrders) {
            if (!"OPEN".equals(buyOrder.getStatus())) {
                continue; // 已成交或取消
            }
            
            for (TradeOrder sellOrder : sellOrders) {
                if (!"OPEN".equals(sellOrder.getStatus())) {
                    continue; // 已成交或取消
                }
                
                // 不能自己和自己交易
                if (buyOrder.getUserId().equals(sellOrder.getUserId())) {
                    continue;
                }
                
                // 买单价格 >= 卖单价格才能成交
                if (buyOrder.getPrice().compareTo(sellOrder.getPrice()) >= 0) {
                    // 执行撮合
                    matchTwoOrders(buyOrder, sellOrder);
                    
                    // 如果买单已完全成交，跳出内层循环
                    if ("CLOSED".equals(buyOrder.getStatus())) {
                        break;
                    }
                } else {
                    // 由于卖单按价格升序，后面的卖单价格更高，不可能成交
                    break;
                }
            }
        }
    }
    
    /**
     * 撮合两个订单
     */
    private void matchTwoOrders(TradeOrder buyOrder, TradeOrder sellOrder) {
        // 计算可成交数量
        BigDecimal buyRemaining = buyOrder.getQuantity().subtract(buyOrder.getTradedQuantity());
        BigDecimal sellRemaining = sellOrder.getQuantity().subtract(sellOrder.getTradedQuantity());
        
        BigDecimal matchQuantity = buyRemaining.min(sellRemaining);
        
        // 成交价格：取卖单价格（对买方更有利）
        BigDecimal matchPrice = sellOrder.getPrice();
        BigDecimal totalAmount = matchQuantity.multiply(matchPrice);
        
        // 更新买单
        buyOrder.setTradedQuantity(buyOrder.getTradedQuantity().add(matchQuantity));
        buyOrder.setMatchedOrderId(sellOrder.getId());
        buyOrder.setMatchTime(LocalDateTime.now());
        buyOrder.setFinalPrice(matchPrice);
        
        if (buyOrder.getTradedQuantity().compareTo(buyOrder.getQuantity()) >= 0) {
            buyOrder.setStatus("CLOSED");
        }
        
        tradeOrderMapper.updateById(buyOrder);
        
        // 更新卖单
        sellOrder.setTradedQuantity(sellOrder.getTradedQuantity().add(matchQuantity));
        sellOrder.setMatchedOrderId(buyOrder.getId());
        sellOrder.setMatchTime(LocalDateTime.now());
        sellOrder.setFinalPrice(matchPrice);
        
        if (sellOrder.getTradedQuantity().compareTo(sellOrder.getQuantity()) >= 0) {
            sellOrder.setStatus("CLOSED");
        }
        
        tradeOrderMapper.updateById(sellOrder);
        
        log.info("撮合成功：买单ID={}, 卖单ID={}, 数量={}, 价格={}", 
                 buyOrder.getId(), sellOrder.getId(), matchQuantity, matchPrice);
        
        // 保存交易记录
        saveTradeRecord(buyOrder, sellOrder, matchQuantity, matchPrice, totalAmount);
        
        // 更新账户余额和资产持仓
        updateAccountAndAssets(buyOrder, sellOrder, matchQuantity, totalAmount);
    }
    
    /**
     * 保存交易记录
     */
    private void saveTradeRecord(TradeOrder buyOrder, TradeOrder sellOrder, 
                                  BigDecimal quantity, BigDecimal price, BigDecimal amount) {
        TradeRecord record = new TradeRecord();
        record.setBuyOrderId(buyOrder.getId());
        record.setSellOrderId(sellOrder.getId());
        record.setBuyerId(buyOrder.getUserId());
        record.setSellerId(sellOrder.getUserId());
        record.setAssetType(buyOrder.getItemType());
        record.setTradePrice(price);
        record.setTradeQuantity(quantity);
        record.setTradeAmount(amount);
        record.setTradeTime(LocalDateTime.now());
        
        tradeRecordService.save(record);
        log.info("交易记录已保存：记录ID={}", record.getId());
    }
    
    /**
     * 更新账户余额和资产持仓
     */
    private void updateAccountAndAssets(TradeOrder buyOrder, TradeOrder sellOrder, 
                                        BigDecimal quantity, BigDecimal totalAmount) {
        try {
            // 1. 买方：扣除冻结资金
            boolean buyerDeduct = tradeAccountService.deductFrozenAmount(buyOrder.getUserId(), totalAmount);
            if (!buyerDeduct) {
                log.error("买方扣除冻结资金失败：userId={}, amount={}", buyOrder.getUserId(), totalAmount);
            }
            
            // 2. 卖方：增加余额
            boolean sellerAdd = tradeAccountService.recharge(sellOrder.getUserId(), totalAmount);
            if (!sellerAdd) {
                log.error("卖方增加余额失败：userId={}, amount={}", sellOrder.getUserId(), totalAmount);
            }
            
            // 3. 更新资产持仓（需要通过HTTP调用service-assets）
            // 这里暂时简化处理，实际应该根据assetType调用不同的API
            log.info("TODO: 更新资产持仓 - 买方增加 {} 数量={}, 卖方减少 {} 数量={}", 
                     buyOrder.getItemType(), quantity, sellOrder.getItemType(), quantity);
            
        } catch (Exception e) {
            log.error("更新账户余额和资产持仓失败", e);
        }
    }
}
