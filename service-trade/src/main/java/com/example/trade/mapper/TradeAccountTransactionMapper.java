package com.example.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.trade.entity.TradeAccountTransaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 交易账户交易记录Mapper
 */
@Mapper
public interface TradeAccountTransactionMapper extends BaseMapper<TradeAccountTransaction> {
}