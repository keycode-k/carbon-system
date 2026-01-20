package com.example.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.trade.entity.TradeAccountTransaction;

import java.util.List;

/**
 * 交易账户交易记录服务
 */
public interface TradeAccountTransactionService extends IService<TradeAccountTransaction> {

    /**
     * 根据用户ID获取交易记录
     * @param userId 用户ID
     * @return 交易记录列表
     */
    List<TradeAccountTransaction> getByUserId(Long userId);

    /**
     * 根据账户ID获取交易记录
     * @param accountId 账户ID
     * @return 交易记录列表
     */
    List<TradeAccountTransaction> getByAccountId(Long accountId);

    /**
     * 记录充值交易
     * @param userId 用户ID
     * @param accountId 账户ID
     * @param amount 金额
     * @param balanceBefore 交易前余额
     * @param balanceAfter 交易后余额
     * @param remark 备注
     * @return 交易记录
     */
    TradeAccountTransaction recordRecharge(Long userId, Long accountId, java.math.BigDecimal amount, 
                                          java.math.BigDecimal balanceBefore, java.math.BigDecimal balanceAfter, 
                                          String remark);

    /**
     * 记录提现交易
     * @param userId 用户ID
     * @param accountId 账户ID
     * @param amount 金额
     * @param balanceBefore 交易前余额
     * @param balanceAfter 交易后余额
     * @param remark 备注
     * @return 交易记录
     */
    TradeAccountTransaction recordWithdraw(Long userId, Long accountId, java.math.BigDecimal amount, 
                                           java.math.BigDecimal balanceBefore, java.math.BigDecimal balanceAfter, 
                                           String remark);
}