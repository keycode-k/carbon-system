package com.example.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.trade.entity.TradeAccountTransaction;
import com.example.trade.mapper.TradeAccountTransactionMapper;
import com.example.trade.service.TradeAccountTransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易账户交易记录服务实现
 */
@Service
public class TradeAccountTransactionServiceImpl extends ServiceImpl<TradeAccountTransactionMapper, TradeAccountTransaction> implements TradeAccountTransactionService {

    @Override
    public List<TradeAccountTransaction> getByUserId(Long userId) {
        LambdaQueryWrapper<TradeAccountTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeAccountTransaction::getUserId, userId)
               .orderByDesc(TradeAccountTransaction::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public List<TradeAccountTransaction> getByAccountId(Long accountId) {
        LambdaQueryWrapper<TradeAccountTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeAccountTransaction::getAccountId, accountId)
               .orderByDesc(TradeAccountTransaction::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    @Transactional
    public TradeAccountTransaction recordRecharge(Long userId, Long accountId, java.math.BigDecimal amount, 
                                                 java.math.BigDecimal balanceBefore, java.math.BigDecimal balanceAfter, 
                                                 String remark) {
        TradeAccountTransaction transaction = new TradeAccountTransaction();
        transaction.setUserId(userId);
        transaction.setAccountId(accountId);
        transaction.setType("recharge");
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setRemark(remark);
        transaction.setCreateTime(LocalDateTime.now());

        this.save(transaction);
        return transaction;
    }

    @Override
    @Transactional
    public TradeAccountTransaction recordWithdraw(Long userId, Long accountId, java.math.BigDecimal amount, 
                                                  java.math.BigDecimal balanceBefore, java.math.BigDecimal balanceAfter, 
                                                  String remark) {
        TradeAccountTransaction transaction = new TradeAccountTransaction();
        transaction.setUserId(userId);
        transaction.setAccountId(accountId);
        transaction.setType("withdraw");
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setRemark(remark);
        transaction.setCreateTime(LocalDateTime.now());

        this.save(transaction);
        return transaction;
    }
}