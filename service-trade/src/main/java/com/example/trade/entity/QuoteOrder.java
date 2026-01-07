package com.example.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 询报价单实体类
 */
@Data
@TableName("quote_order")
public class QuoteOrder {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 订单类型：BUY-询价买入，SELL-询价卖出
     */
    private String type;
    
    /**
     * 资产类型：QUOTA-配额，CREDIT-信用
     */
    private String assetType;
    
    /**
     * 数量（吨）
     */
    private BigDecimal quantity;
    
    /**
     * 期望价格（元/吨）
     */
    private BigDecimal expectPrice;
    
    /**
     * 最终成交价格（元/吨）
     */
    private BigDecimal finalPrice;
    
    /**
     * 状态：PENDING-待报价，QUOTED-已报价，ACCEPTED-已接受，REJECTED-已拒绝，CANCELLED-已取消，COMPLETED-已完成
     */
    private String status;
    
    /**
     * 有效期至
     */
    private LocalDateTime validUntil;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
