package com.example.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易成交记录实体
 */
@Data
@TableName("trade_record")
public class TradeRecord {
    /**
     * 交易记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 买单ID
     */
    private Long buyOrderId;

    /**
     * 卖单ID
     */
    private Long sellOrderId;

    /**
     * 买方用户ID
     */
    private Long buyerId;

    /**
     * 卖方用户ID
     */
    private Long sellerId;

    /**
     * 资产类型：QUOTA-碳配额，CREDIT-CCER
     */
    private String assetType;

    /**
     * 成交单价
     */
    private BigDecimal tradePrice;

    /**
     * 成交数量
     */
    private BigDecimal tradeQuantity;

    /**
     * 成交总金额
     */
    private BigDecimal tradeAmount;

    /**
     * 成交时间
     */
    private LocalDateTime tradeTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
