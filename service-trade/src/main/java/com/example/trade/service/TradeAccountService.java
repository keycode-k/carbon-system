package com.example.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.trade.entity.TradeAccount;

import java.math.BigDecimal;

/**
 * 交易账户服务接口
 */
public interface TradeAccountService extends IService<TradeAccount> {

    /**
     * 根据用户ID获取账户信息
     * @param userId 用户ID
     * @return 账户信息
     */
    TradeAccount getByUserId(Long userId);

    /**
     * 初始化账户（首次开通）
     * @param userId 用户ID
     * @return 账户信息
     */
    TradeAccount initAccount(Long userId);

    /**
     * 充值
     * @param userId 用户ID
     * @param amount 充值金额
     * @return 是否成功
     */
    boolean recharge(Long userId, BigDecimal amount);

    /**
     * 提现
     * @param userId 用户ID
     * @param amount 提现金额
     * @return 是否成功
     */
    boolean withdraw(Long userId, BigDecimal amount);

    /**
     * 冻结资金（下单时）
     * @param userId 用户ID
     * @param amount 冻结金额
     * @return 是否成功
     */
    boolean freezeAmount(Long userId, BigDecimal amount);

    /**
     * 解冻资金（撤单时）
     * @param userId 用户ID
     * @param amount 解冻金额
     * @return 是否成功
     */
    boolean unfreezeAmount(Long userId, BigDecimal amount);

    /**
     * 扣除冻结资金（成交时）
     * @param userId 用户ID
     * @param amount 扣除金额
     * @return 是否成功
     */
    boolean deductFrozenAmount(Long userId, BigDecimal amount);
}
