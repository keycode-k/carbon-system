package com.example.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.trade.entity.QuoteOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 询报价单 Mapper 接口
 */
@Mapper
public interface QuoteOrderMapper extends BaseMapper<QuoteOrder> {
}
