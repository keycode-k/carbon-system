package com.example.trade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.trade.entity.TradeOrder;
import com.example.trade.mapper.TradeOrderMapper;
import com.example.trade.service.TradeOrderService;
import org.springframework.stereotype.Service;

@Service
public class TradeOrderServiceImpl extends ServiceImpl<TradeOrderMapper, TradeOrder> implements TradeOrderService {
}
