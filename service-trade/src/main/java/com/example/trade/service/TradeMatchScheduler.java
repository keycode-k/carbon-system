package com.example.trade.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 交易撮合定时任务
 * 每5秒自动执行一次撮合
 */
@Slf4j
@Component
public class TradeMatchScheduler {
    
    @Autowired
    private TradeMatchService tradeMatchService;
    
    /**
     * 定时执行撮合任务
     * 每5秒执行一次
     */
    @Scheduled(fixedRate = 5000)
    public void autoMatch() {
        try {
            tradeMatchService.matchOrders();
        } catch (Exception e) {
            log.error("自动撮合任务执行失败", e);
        }
    }
}
