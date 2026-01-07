package com.example.consumer.feign;

import com.example.common.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 交易订单服务Feign客户端
 */
@FeignClient(name = "service-trade", contextId = "tradeOrderFeignClient")
public interface TradeOrderFeignClient {

    /**
     * 获取市场挂单列表
     */
    @GetMapping("/api/trade/orders/market")
    Result<Map<String, Object>> getMarketOrders(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "direction", required = false) String direction
    );

    /**
     * 获取用户订单列表
     */
    @GetMapping("/api/trade/orders/my")
    Result<Map<String, Object>> getMyOrders(
            @RequestParam(value = "userId") Long userId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size
    );
}
