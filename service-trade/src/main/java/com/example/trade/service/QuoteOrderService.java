package com.example.trade.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.trade.entity.QuoteOrder;

import java.math.BigDecimal;

/**
 * 询报价单服务接口
 */
public interface QuoteOrderService extends IService<QuoteOrder> {
    
    /**
     * 分页查询询报价单
     * @param page 页码
     * @param size 每页大小
     * @param type 订单类型
     * @param assetType 资产类型
     * @param status 状态
     * @return 分页结果
     */
    Page<QuoteOrder> pageQuery(Integer page, Integer size, String type, String assetType, String status);
    
    /**
     * 查询我的询报价单
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    Page<QuoteOrder> getMyQuotes(Long userId, Integer page, Integer size);
    
    /**
     * 发布询价单
     * @param quoteOrder 询价单信息
     * @return 是否成功
     */
    boolean publishQuote(QuoteOrder quoteOrder);
    
    /**
     * 报价（商家对询价单进行报价）
     * @param quoteId 询价单ID
     * @param finalPrice 报价
     * @return 是否成功
     */
    boolean makeQuote(Long quoteId, BigDecimal finalPrice);
    
    /**
     * 接受报价
     * @param quoteId 询价单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean acceptQuote(Long quoteId, Long userId);
    
    /**
     * 拒绝报价
     * @param quoteId 询价单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean rejectQuote(Long quoteId, Long userId);
    
    /**
     * 取消询价单
     * @param quoteId 询价单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean cancelQuote(Long quoteId, Long userId);
}
