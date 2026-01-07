package com.example.consumer.feign;

import com.example.common.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 交易账户服务Feign客户端
 */
@FeignClient(name = "service-trade", path = "/api/trade/account")
public interface TradeAccountFeignClient {

    /**
     * 获取账户信息
     */
    @GetMapping("/info/{userId}")
    Result<Map<String, Object>> getAccountInfo(@PathVariable("userId") Long userId);

    /**
     * 初始化账户
     */
    @PostMapping("/init")
    Result<Map<String, Object>> initAccount(@RequestParam("userId") Long userId);

    /**
     * 充值
     */
    @PostMapping("/recharge")
    Result<String> recharge(@RequestParam("userId") Long userId, @RequestParam("amount") BigDecimal amount);

    /**
     * 提现
     */
    @PostMapping("/withdraw")
    Result<String> withdraw(@RequestParam("userId") Long userId, @RequestParam("amount") BigDecimal amount);

    /**
     * 冻结资金
     */
    @PostMapping("/freeze")
    Result<String> freezeAmount(@RequestParam("userId") Long userId, @RequestParam("amount") BigDecimal amount);

    /**
     * 解冻资金
     */
    @PostMapping("/unfreeze")
    Result<String> unfreezeAmount(@RequestParam("userId") Long userId, @RequestParam("amount") BigDecimal amount);

    /**
     * 扣除冻结资金
     */
    @PostMapping("/deduct")
    Result<String> deductFrozenAmount(@RequestParam("userId") Long userId, @RequestParam("amount") BigDecimal amount);
}
