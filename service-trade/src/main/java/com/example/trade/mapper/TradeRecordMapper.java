package com.example.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.trade.entity.TradeRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 交易记录Mapper
 */
@Mapper
public interface TradeRecordMapper extends BaseMapper<TradeRecord> {
}
