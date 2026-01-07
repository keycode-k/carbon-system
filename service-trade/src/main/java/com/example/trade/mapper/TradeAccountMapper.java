package com.example.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.trade.entity.TradeAccount;
import org.apache.ibatis.annotations.Mapper;

/**
 * 交易账户Mapper接口
 */
@Mapper
public interface TradeAccountMapper extends BaseMapper<TradeAccount> {
}
