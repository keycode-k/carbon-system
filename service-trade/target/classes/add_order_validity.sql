-- 为trade_order表添加有效期字段
ALTER TABLE trade_order ADD COLUMN valid_days INT DEFAULT 7 COMMENT '订单有效期（天数）' AFTER final_price;
ALTER TABLE trade_order ADD COLUMN expire_time DATETIME COMMENT '订单到期时间' AFTER valid_days;

-- 为已有订单设置过期时间（创建时间+7天）
UPDATE trade_order 
SET expire_time = DATE_ADD(create_time, INTERVAL 7 DAY)
WHERE expire_time IS NULL AND status = 'OPEN';
