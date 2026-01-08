-- ========================================
-- 交易撮合引擎优化 - 数据库迁移脚本
-- 执行数据库: carbondate
-- 执行日期: 2026-01-07
-- ========================================

USE carbondate;

-- 1. 创建交易记录表
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

-- 2. 为trade_order表添加有效期字段（如果字段不存在）
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_SCHEMA = 'carbondate' 
                   AND TABLE_NAME = 'trade_order' 
                   AND COLUMN_NAME = 'valid_days');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE trade_order ADD COLUMN valid_days INT DEFAULT 7 COMMENT ''订单有效期（天数）'' AFTER final_price',
    'SELECT ''Column valid_days already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_SCHEMA = 'carbondate' 
                   AND TABLE_NAME = 'trade_order' 
                   AND COLUMN_NAME = 'expire_time');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE trade_order ADD COLUMN expire_time DATETIME COMMENT ''订单到期时间'' AFTER valid_days',
    'SELECT ''Column expire_time already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 为已有订单设置过期时间（创建时间+7天）
UPDATE trade_order 
SET expire_time = DATE_ADD(create_time, INTERVAL 7 DAY),
    valid_days = 7
WHERE expire_time IS NULL AND status = 'OPEN';

-- 验证结果
SELECT 'trade_record 表创建成功' AS message;
SELECT COUNT(*) AS trade_record_count FROM trade_record;
SELECT 'trade_order 字段添加成功' AS message;
SHOW COLUMNS FROM trade_order LIKE '%expire%';
SHOW COLUMNS FROM trade_order LIKE '%valid%';
