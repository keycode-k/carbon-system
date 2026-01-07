package com.example.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易账户实体类
 */
@Data
@TableName("trade_account")
public class TradeAccount {

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
     * 账户余额（可用资金）
     */
    private BigDecimal balance;

    /**
     * 冻结金额（挂单占用）
     */
    private BigDecimal frozenAmount;

    /**
     * 总资产（余额+冻结）
     */
    private BigDecimal totalAssets;

    /**
     * 累计充值
     */
    private BigDecimal totalRecharge;

    /**
     * 累计提现
     */
    private BigDecimal totalWithdraw;

    /**
     * 账户状态：0-正常，1-冻结，2-注销
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
