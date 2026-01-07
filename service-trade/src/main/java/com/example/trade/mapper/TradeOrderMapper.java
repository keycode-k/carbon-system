package com.example.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.trade.entity.TradeOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TradeOrderMapper extends BaseMapper<TradeOrder> {
}
