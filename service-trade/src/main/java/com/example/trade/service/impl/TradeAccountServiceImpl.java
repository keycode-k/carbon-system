package com.example.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.trade.entity.TradeAccount;
import com.example.trade.mapper.TradeAccountMapper;
import com.example.trade.service.TradeAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易账户服务实现类
 */
@Service
public class TradeAccountServiceImpl extends ServiceImpl<TradeAccountMapper, TradeAccount> implements TradeAccountService {

    @Override
    public TradeAccount getByUserId(Long userId) {
        LambdaQueryWrapper<TradeAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeAccount::getUserId, userId);
        return this.getOne(wrapper);
    }

    @Override
    @Transactional
    public TradeAccount initAccount(Long userId) {
        // 检查是否已存在账户
        TradeAccount existing = getByUserId(userId);
        if (existing != null) {
            return existing;
        }

        // 创建新账户
        TradeAccount account = new TradeAccount();
        account.setUserId(userId);
        account.setBalance(BigDecimal.ZERO);
        account.setFrozenAmount(BigDecimal.ZERO);
        account.setTotalAssets(BigDecimal.ZERO);
        account.setTotalRecharge(BigDecimal.ZERO);
        account.setTotalWithdraw(BigDecimal.ZERO);
        account.setStatus(0);
        account.setCreateTime(LocalDateTime.now());
        account.setUpdateTime(LocalDateTime.now());

        this.save(account);
        return account;
    }

    @Override
    @Transactional
    public boolean recharge(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        TradeAccount account = getByUserId(userId);
        if (account == null) {
            account = initAccount(userId);
        }

        // 更新余额
        account.setBalance(account.getBalance().add(amount));
        account.setTotalAssets(account.getBalance().add(account.getFrozenAmount()));
        account.setTotalRecharge(account.getTotalRecharge().add(amount));
        account.setUpdateTime(LocalDateTime.now());

        return this.updateById(account);
    }

    @Override
    @Transactional
    public boolean withdraw(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        TradeAccount account = getByUserId(userId);
        if (account == null || account.getBalance().compareTo(amount) < 0) {
            return false;
        }

        // 更新余额
        account.setBalance(account.getBalance().subtract(amount));
        account.setTotalAssets(account.getBalance().add(account.getFrozenAmount()));
        account.setTotalWithdraw(account.getTotalWithdraw().add(amount));
        account.setUpdateTime(LocalDateTime.now());

        return this.updateById(account);
    }

    @Override
    @Transactional
    public boolean freezeAmount(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        TradeAccount account = getByUserId(userId);
        if (account == null || account.getBalance().compareTo(amount) < 0) {
            return false;
        }

        // 从可用余额转到冻结金额
        account.setBalance(account.getBalance().subtract(amount));
        account.setFrozenAmount(account.getFrozenAmount().add(amount));
        account.setUpdateTime(LocalDateTime.now());

        return this.updateById(account);
    }

    @Override
    @Transactional
    public boolean unfreezeAmount(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        TradeAccount account = getByUserId(userId);
        if (account == null || account.getFrozenAmount().compareTo(amount) < 0) {
            return false;
        }

        // 从冻结金额转回可用余额
        account.setFrozenAmount(account.getFrozenAmount().subtract(amount));
        account.setBalance(account.getBalance().add(amount));
        account.setUpdateTime(LocalDateTime.now());

        return this.updateById(account);
    }

    @Override
    @Transactional
    public boolean deductFrozenAmount(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        TradeAccount account = getByUserId(userId);
        if (account == null || account.getFrozenAmount().compareTo(amount) < 0) {
            return false;
        }

        // 直接扣除冻结金额
        account.setFrozenAmount(account.getFrozenAmount().subtract(amount));
        account.setTotalAssets(account.getBalance().add(account.getFrozenAmount()));
        account.setUpdateTime(LocalDateTime.now());

        return this.updateById(account);
    }
}
