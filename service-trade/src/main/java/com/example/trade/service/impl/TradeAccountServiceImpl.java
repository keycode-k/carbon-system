package com.example.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.trade.entity.TradeAccount;
import com.example.trade.entity.TradeAccountTransaction;
import com.example.trade.mapper.TradeAccountMapper;
import com.example.trade.service.TradeAccountService;
import com.example.trade.service.TradeAccountTransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易账户服务实现类
 */
@Service
public class TradeAccountServiceImpl extends ServiceImpl<TradeAccountMapper, TradeAccount> implements TradeAccountService {

    @Autowired
    private TradeAccountTransactionService transactionService;

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

        // 记录交易前余额
        BigDecimal balanceBefore = account.getBalance();
        
        // 更新余额
        account.setBalance(account.getBalance().add(amount));
        account.setTotalAssets(account.getBalance().add(account.getFrozenAmount()));
        account.setTotalRecharge(account.getTotalRecharge().add(amount));
        account.setUpdateTime(LocalDateTime.now());

        boolean success = this.updateById(account);
        
        // 记录交易
        if (success) {
            transactionService.recordRecharge(userId, account.getId(), amount, balanceBefore, account.getBalance(), 
                                             "用户充值");
        }
        
        return success;
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

        // 记录交易前余额
        BigDecimal balanceBefore = account.getBalance();
        
        // 更新余额
        account.setBalance(account.getBalance().subtract(amount));
        account.setTotalAssets(account.getBalance().add(account.getFrozenAmount()));
        account.setTotalWithdraw(account.getTotalWithdraw().add(amount));
        account.setUpdateTime(LocalDateTime.now());

        boolean success = this.updateById(account);
        
        // 记录交易
        if (success) {
            transactionService.recordWithdraw(userId, account.getId(), amount, balanceBefore, account.getBalance(), 
                                              "用户提现");
        }
        
        return success;
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

        // 记录交易前余额
        BigDecimal balanceBefore = account.getBalance();
        BigDecimal frozenBefore = account.getFrozenAmount();
        
        // 从可用余额转到冻结金额
        account.setBalance(account.getBalance().subtract(amount));
        account.setFrozenAmount(account.getFrozenAmount().add(amount));
        account.setUpdateTime(LocalDateTime.now());

        boolean success = this.updateById(account);
        
        // 记录交易
        if (success) {
            // 创建冻结交易记录
            TradeAccountTransaction transaction = new TradeAccountTransaction();
            transaction.setUserId(userId);
            transaction.setAccountId(account.getId());
            transaction.setType("freeze");
            transaction.setAmount(amount);
            transaction.setBalanceBefore(balanceBefore);
            transaction.setBalanceAfter(account.getBalance());
            transaction.setRemark("资金冻结");
            transaction.setCreateTime(LocalDateTime.now());
            transactionService.save(transaction);
        }
        
        return success;
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

        // 记录交易前余额
        BigDecimal balanceBefore = account.getBalance();
        BigDecimal frozenBefore = account.getFrozenAmount();
        
        // 从冻结金额转回可用余额
        account.setFrozenAmount(account.getFrozenAmount().subtract(amount));
        account.setBalance(account.getBalance().add(amount));
        account.setUpdateTime(LocalDateTime.now());

        boolean success = this.updateById(account);
        
        // 记录交易
        if (success) {
            // 创建解冻交易记录
            TradeAccountTransaction transaction = new TradeAccountTransaction();
            transaction.setUserId(userId);
            transaction.setAccountId(account.getId());
            transaction.setType("unfreeze");
            transaction.setAmount(amount);
            transaction.setBalanceBefore(balanceBefore);
            transaction.setBalanceAfter(account.getBalance());
            transaction.setRemark("资金解冻");
            transaction.setCreateTime(LocalDateTime.now());
            transactionService.save(transaction);
        }
        
        return success;
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

        // 记录交易前余额
        BigDecimal frozenBefore = account.getFrozenAmount();
        BigDecimal totalBefore = account.getBalance().add(account.getFrozenAmount());
        
        // 直接扣除冻结金额
        account.setFrozenAmount(account.getFrozenAmount().subtract(amount));
        account.setTotalAssets(account.getBalance().add(account.getFrozenAmount()));
        account.setUpdateTime(LocalDateTime.now());

        boolean success = this.updateById(account);
        
        // 记录交易
        if (success) {
            // 创建扣除交易记录
            TradeAccountTransaction transaction = new TradeAccountTransaction();
            transaction.setUserId(userId);
            transaction.setAccountId(account.getId());
            transaction.setType("deduct");
            transaction.setAmount(amount);
            transaction.setBalanceBefore(totalBefore);
            transaction.setBalanceAfter(account.getTotalAssets());
            transaction.setRemark("扣除冻结资金");
            transaction.setCreateTime(LocalDateTime.now());
            transactionService.save(transaction);
        }
        
        return success;
    }
}
