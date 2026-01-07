package com.example.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("trade_order")
public class TradeOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId; // 发起交易的用户ID

    private String itemType; // 商品类型: QUOTA (配额), CREDIT (碳信用)

    private Long itemId; // 商品ID (如果是出售)

    private String direction; // 方向: BUY (买入), SELL (卖出)

    private BigDecimal price; // 单价

    private BigDecimal quantity; // 数量

    private BigDecimal tradedQuantity; // 已成交数量

    private String status; // 状态: OPEN (挂单中), CLOSED (已结束), CANCELLED (已撤销)

    private Long matchedOrderId; // 匹配的订单ID
    
    private LocalDateTime matchTime; // 成交时间
    
    private BigDecimal finalPrice; // 最终成交价
    
    private Integer validDays; // 订单有效期（天数），默认7天
    
    private LocalDateTime expireTime; // 订单到期时间

    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
