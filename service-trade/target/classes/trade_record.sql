-- 交易记录表
CREATE TABLE IF NOT EXISTS trade_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '交易记录ID',
    buy_order_id BIGINT NOT NULL COMMENT '买单ID',
    sell_order_id BIGINT NOT NULL COMMENT '卖单ID',
    buyer_id BIGINT NOT NULL COMMENT '买方用户ID',
    seller_id BIGINT NOT NULL COMMENT '卖方用户ID',
    asset_type VARCHAR(20) NOT NULL COMMENT '资产类型：QUOTA-碳配额，CREDIT-CCER',
    trade_price DECIMAL(10, 2) NOT NULL COMMENT '成交单价',
    trade_quantity DECIMAL(15, 2) NOT NULL COMMENT '成交数量',
    trade_amount DECIMAL(20, 2) NOT NULL COMMENT '成交总金额',
    trade_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '成交时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_buyer_id (buyer_id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_trade_time (trade_time),
    INDEX idx_buy_order (buy_order_id),
    INDEX idx_sell_order (sell_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易成交记录表';
