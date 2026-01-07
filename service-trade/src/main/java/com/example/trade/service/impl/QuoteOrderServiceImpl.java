package com.example.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.trade.entity.QuoteOrder;
import com.example.trade.mapper.QuoteOrderMapper;
import com.example.trade.service.QuoteOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 询报价单服务实现类
 */
@Service
public class QuoteOrderServiceImpl extends ServiceImpl<QuoteOrderMapper, QuoteOrder> implements QuoteOrderService {
    
    @Override
    public Page<QuoteOrder> pageQuery(Integer page, Integer size, String type, String assetType, String status) {
        Page<QuoteOrder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<QuoteOrder> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询待报价和已报价的订单（市场公开询价单）
        if (status != null && !status.isEmpty()) {
            wrapper.eq(QuoteOrder::getStatus, status);
        } else {
            wrapper.in(QuoteOrder::getStatus, "PENDING", "QUOTED");
        }
        
        if (type != null && !type.isEmpty()) {
            wrapper.eq(QuoteOrder::getType, type);
        }
        
        if (assetType != null && !assetType.isEmpty()) {
            wrapper.eq(QuoteOrder::getAssetType, assetType);
        }
        
        // 过滤已过期的询价单
        wrapper.and(w -> w.isNull(QuoteOrder::getValidUntil)
                .or()
                .gt(QuoteOrder::getValidUntil, LocalDateTime.now()));
        
        wrapper.orderByDesc(QuoteOrder::getCreateTime);
        
        return this.page(pageParam, wrapper);
    }
    
    @Override
    public Page<QuoteOrder> getMyQuotes(Long userId, Integer page, Integer size) {
        Page<QuoteOrder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<QuoteOrder> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(QuoteOrder::getUserId, userId);
        wrapper.orderByDesc(QuoteOrder::getCreateTime);
        
        return this.page(pageParam, wrapper);
    }
    
    @Override
    @Transactional
    public boolean publishQuote(QuoteOrder quoteOrder) {
        quoteOrder.setStatus("PENDING");
        quoteOrder.setCreateTime(LocalDateTime.now());
        quoteOrder.setUpdateTime(LocalDateTime.now());
        
        // 如果没有设置有效期，默认7天
        if (quoteOrder.getValidUntil() == null) {
            quoteOrder.setValidUntil(LocalDateTime.now().plusDays(7));
        }
        
        return this.save(quoteOrder);
    }
    
    @Override
    @Transactional
    public boolean makeQuote(Long quoteId, BigDecimal finalPrice) {
        QuoteOrder quoteOrder = this.getById(quoteId);
        if (quoteOrder == null) {
            throw new RuntimeException("询价单不存在");
        }
        
        if (!"PENDING".equals(quoteOrder.getStatus())) {
            throw new RuntimeException("询价单状态不正确");
        }
        
        // 检查是否过期
        if (quoteOrder.getValidUntil() != null && quoteOrder.getValidUntil().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("询价单已过期");
        }
        
        quoteOrder.setFinalPrice(finalPrice);
        quoteOrder.setStatus("QUOTED");
        quoteOrder.setUpdateTime(LocalDateTime.now());
        
        return this.updateById(quoteOrder);
    }
    
    @Override
    @Transactional
    public boolean acceptQuote(Long quoteId, Long userId) {
        QuoteOrder quoteOrder = this.getById(quoteId);
        if (quoteOrder == null) {
            throw new RuntimeException("询价单不存在");
        }
        
        if (!quoteOrder.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此询价单");
        }
        
        if (!"QUOTED".equals(quoteOrder.getStatus())) {
            throw new RuntimeException("询价单状态不正确");
        }
        
        quoteOrder.setStatus("ACCEPTED");
        quoteOrder.setUpdateTime(LocalDateTime.now());
        
        // TODO: 这里可以触发实际交易流程，创建交易订单
        
        return this.updateById(quoteOrder);
    }
    
    @Override
    @Transactional
    public boolean rejectQuote(Long quoteId, Long userId) {
        QuoteOrder quoteOrder = this.getById(quoteId);
        if (quoteOrder == null) {
            throw new RuntimeException("询价单不存在");
        }
        
        if (!quoteOrder.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此询价单");
        }
        
        if (!"QUOTED".equals(quoteOrder.getStatus())) {
            throw new RuntimeException("询价单状态不正确");
        }
        
        quoteOrder.setStatus("REJECTED");
        quoteOrder.setUpdateTime(LocalDateTime.now());
        
        return this.updateById(quoteOrder);
    }
    
    @Override
    @Transactional
    public boolean cancelQuote(Long quoteId, Long userId) {
        QuoteOrder quoteOrder = this.getById(quoteId);
        if (quoteOrder == null) {
            throw new RuntimeException("询价单不存在");
        }
        
        if (!quoteOrder.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此询价单");
        }
        
        if ("ACCEPTED".equals(quoteOrder.getStatus()) || "COMPLETED".equals(quoteOrder.getStatus())) {
            throw new RuntimeException("询价单已完成，无法取消");
        }
        
        quoteOrder.setStatus("CANCELLED");
        quoteOrder.setUpdateTime(LocalDateTime.now());
        
        return this.updateById(quoteOrder);
    }
}
