package com.example.trade.controller;

import com.example.common.model.Result;
import com.example.trade.entity.TradeAccount;
import com.example.trade.service.TradeAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 交易账户控制器
 */
@RestController
@RequestMapping("/api/trade/account")
public class TradeAccountController {

    @Autowired
    private TradeAccountService tradeAccountService;

    /**
     * 获取账户信息
     */
    @GetMapping("/info/{userId}")
    public Result<TradeAccount> getAccountInfo(@PathVariable Long userId) {
        TradeAccount account = tradeAccountService.getByUserId(userId);
        if (account == null) {
            // 自动初始化账户
            account = tradeAccountService.initAccount(userId);
        }
        return Result.success(account);
    }

    /**
     * 初始化账户
     */
    @PostMapping("/init")
    public Result<TradeAccount> initAccount(@RequestParam Long userId) {
        TradeAccount account = tradeAccountService.initAccount(userId);
        return Result.success(account);
    }

    /**
     * 充值
     */
    @PostMapping("/recharge")
    public Result<String> recharge(@RequestParam Long userId, @RequestParam BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("充值金额必须大于0");
        }

        boolean success = tradeAccountService.recharge(userId, amount);
        return success ? Result.success("充值成功") : Result.error("充值失败");
    }

    /**
     * 提现
     */
    @PostMapping("/withdraw")
    public Result<String> withdraw(@RequestParam Long userId, @RequestParam BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("提现金额必须大于0");
        }

        TradeAccount account = tradeAccountService.getByUserId(userId);
        if (account == null || account.getBalance().compareTo(amount) < 0) {
            return Result.error("余额不足");
        }

        boolean success = tradeAccountService.withdraw(userId, amount);
        return success ? Result.success("提现成功") : Result.error("提现失败");
    }

    /**
     * 冻结资金（内部接口，供订单服务调用）
     */
    @PostMapping("/freeze")
    public Result<String> freezeAmount(@RequestParam Long userId, @RequestParam BigDecimal amount) {
        boolean success = tradeAccountService.freezeAmount(userId, amount);
        return success ? Result.success("冻结成功") : Result.error("余额不足或冻结失败");
    }

    /**
     * 解冻资金（内部接口，供订单服务调用）
     */
    @PostMapping("/unfreeze")
    public Result<String> unfreezeAmount(@RequestParam Long userId, @RequestParam BigDecimal amount) {
        boolean success = tradeAccountService.unfreezeAmount(userId, amount);
        return success ? Result.success("解冻成功") : Result.error("解冻失败");
    }

    /**
     * 扣除冻结资金（内部接口，供订单服务调用）
     */
    @PostMapping("/deduct")
    public Result<String> deductFrozenAmount(@RequestParam Long userId, @RequestParam BigDecimal amount) {
        boolean success = tradeAccountService.deductFrozenAmount(userId, amount);
        return success ? Result.success("扣除成功") : Result.error("扣除失败");
    }

    /**
     * 获取账户统计信息
     */
    @GetMapping("/statistics/{userId}")
    public Result<TradeAccount> getStatistics(@PathVariable Long userId) {
        TradeAccount account = tradeAccountService.getByUserId(userId);
        if (account == null) {
            return Result.error("账户不存在");
        }
        return Result.success(account);
    }
}
