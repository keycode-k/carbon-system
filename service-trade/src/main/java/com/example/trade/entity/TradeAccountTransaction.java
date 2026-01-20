package com.example.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易账户交易记录实体
 */
@Data
@Accessors(chain = true)
@TableName("trade_account_transaction")
public class TradeAccountTransaction {

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
     * 账户ID
     */
    private Long accountId;

    /**
     * 交易类型：recharge(充值), withdraw(提现), freeze(冻结), unfreeze(解冻), deduct(扣除)
     */
    private String type;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 交易前余额
     */
    private BigDecimal balanceBefore;

    /**
     * 交易后余额
     */
    private BigDecimal balanceAfter;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}