package com.example.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.trade.entity.TradeRecord;
import com.example.trade.mapper.TradeRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 交易记录服务
 */
@Service
public class TradeRecordService {

    @Autowired
    private TradeRecordMapper tradeRecordMapper;

    /**
     * 保存交易记录
     */
    public void save(TradeRecord record) {
        tradeRecordMapper.insert(record);
    }

    /**
     * 查询用户的交易记录（分页）
     */
    public Page<TradeRecord> getUserTradeRecords(Long userId, int current, int size) {
        Page<TradeRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<TradeRecord> query = new LambdaQueryWrapper<>();
        query.and(wrapper -> wrapper.eq(TradeRecord::getBuyerId, userId)
                                     .or()
                                     .eq(TradeRecord::getSellerId, userId));
        query.orderByDesc(TradeRecord::getTradeTime);
        return tradeRecordMapper.selectPage(page, query);
    }

    /**
     * 查询所有交易记录（分页）
     */
    public Page<TradeRecord> getAllTradeRecords(int current, int size) {
        Page<TradeRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<TradeRecord> query = new LambdaQueryWrapper<>();
        query.orderByDesc(TradeRecord::getTradeTime);
        return tradeRecordMapper.selectPage(page, query);
    }

    /**
     * 根据订单ID查询交易记录
     */
    public List<TradeRecord> getByOrderId(Long orderId) {
        LambdaQueryWrapper<TradeRecord> query = new LambdaQueryWrapper<>();
        query.eq(TradeRecord::getBuyOrderId, orderId)
             .or()
             .eq(TradeRecord::getSellOrderId, orderId);
        return tradeRecordMapper.selectList(query);
    }
}
