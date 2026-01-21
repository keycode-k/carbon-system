-- 交易撮合测试数据
-- 用于测试自动撮合功能

-- 清空现有订单（可选）
-- TRUNCATE TABLE trade_order;

-- 场景1：完全匹配 - 买单价格=卖单价格
-- 买单：用户1想以50元/吨买100吨QUOTA
INSERT INTO `trade_order` (`user_id`, `item_type`, `item_id`, `direction`, `price`, `quantity`, `traded_quantity`, `status`, `create_time`)
VALUES (1, 'QUOTA', NULL, 'BUY', 50.00, 100.00, 0.00, 'OPEN', NOW());

-- 卖单：用户2想以50元/吨卖100吨QUOTA
INSERT INTO `trade_order` (`user_id`, `item_type`, `item_id`, `direction`, `price`, `quantity`, `traded_quantity`, `status`, `create_time`)
VALUES (2, 'QUOTA', NULL, 'SELL', 50.00, 100.00, 0.00, 'OPEN', NOW());

-- 场景2：价格优先 - 买单价格 > 卖单价格（应该成交）
-- 买单：用户1想以60元/吨买200吨CREDIT
INSERT INTO `trade_order` (`user_id`, `item_type`, `item_id`, `direction`, `price`, `quantity`, `traded_quantity`, `status`, `create_time`)
VALUES (1, 'CREDIT', NULL, 'BUY', 60.00, 200.00, 0.00, 'OPEN', NOW());

-- 卖单：用户3想以55元/吨卖150吨CREDIT
INSERT INTO `trade_order` (`user_id`, `item_type`, `item_id`, `direction`, `price`, `quantity`, `traded_quantity`, `status`, `create_time`)
VALUES (3, 'CREDIT', NULL, 'SELL', 55.00, 150.00, 0.00, 'OPEN', NOW());

-- 场景3：部分成交 - 买单数量 > 卖单数量
-- 买单：用户2想以48元/吨买500吨QUOTA
INSERT INTO `trade_order` (`user_id`, `item_type`, `item_id`, `direction`, `price`, `quantity`, `traded_quantity`, `status`, `create_time`)
VALUES (2, 'QUOTA', NULL, 'BUY', 48.00, 500.00, 0.00, 'OPEN', NOW());

-- 卖单：用户3想以45元/吨卖200吨QUOTA
INSERT INTO `trade_order` (`user_id`, `item_type`, `item_id`, `direction`, `price`, `quantity`, `traded_quantity`, `status`, `create_time`)
VALUES (3, 'QUOTA', NULL, 'SELL', 45.00, 200.00, 0.00, 'OPEN', NOW());

-- 场景4：无法成交 - 买单价格 < 卖单价格
-- 买单：用户1想以40元/吨买100吨CREDIT
INSERT INTO `trade_order` (`user_id`, `item_type`, `item_id`, `direction`, `price`, `quantity`, `traded_quantity`, `status`, `create_time`)
VALUES (1, 'CREDIT', NULL, 'BUY', 40.00, 100.00, 0.00, 'OPEN', NOW());

-- 卖单：用户2想以50元/吨卖100吨CREDIT
INSERT INTO `trade_order` (`user_id`, `item_type`, `item_id`, `direction`, `price`, `quantity`, `traded_quantity`, `status`, `create_time`)
VALUES (2, 'CREDIT', NULL, 'SELL', 50.00, 100.00, 0.00, 'OPEN', NOW());

-- 场景5：时间优先 - 多个卖单价格相同
-- 买单：用户3想以52元/吨买300吨QUOTA
INSERT INTO `trade_order` (`user_id`, `item_type`, `item_id`, `direction`, `price`, `quantity`, `traded_quantity`, `status`, `create_time`)
VALUES (3, 'QUOTA', NULL, 'BUY', 52.00, 300.00, 0.00, 'OPEN', NOW());

-- 卖单1：用户1想以50元/吨卖100吨QUOTA（最早）
INSERT INTO `trade_order` (`user_id`, `item_type`, `item_id`, `direction`, `price`, `quantity`, `traded_quantity`, `status`, `create_time`)
VALUES (1, 'QUOTA', NULL, 'SELL', 50.00, 100.00, 0.00, 'OPEN', DATE_SUB(NOW(), INTERVAL 10 SECOND));

-- 卖单2：用户2想以50元/吨卖100吨QUOTA（稍晚）
INSERT INTO `trade_order` (`user_id`, `item_type`, `item_id`, `direction`, `price`, `quantity`, `traded_quantity`, `status`, `create_time`)
VALUES (2, 'QUOTA', NULL, 'SELL', 50.00, 100.00, 0.00, 'OPEN', DATE_SUB(NOW(), INTERVAL 5 SECOND));

-- 场景6：多次部分成交
-- 买单1：用户1想以55元/吨买150吨CREDIT
INSERT INTO `trade_order` (`user_id`, `item_type`, `item_id`, `direction`, `price`, `quantity`, `traded_quantity`, `status`, `create_time`)
VALUES (1, 'CREDIT', NULL, 'BUY', 55.00, 150.00, 0.00, 'OPEN', NOW());

-- 买单2：用户2想以54元/吨买150吨CREDIT
INSERT INTO `trade_order` (`user_id`, `item_type`, `item_id`, `direction`, `price`, `quantity`, `traded_quantity`, `status`, `create_time`)
VALUES (2, 'CREDIT', NULL, 'BUY', 54.00, 150.00, 0.00, 'OPEN', NOW());

-- 卖单：用户3想以52元/吨卖250吨CREDIT（可以部分匹配两个买单）
INSERT INTO `trade_order` (`user_id`, `item_type`, `item_id`, `direction`, `price`, `quantity`, `traded_quantity`, `status`, `create_time`)
VALUES (3, 'CREDIT', NULL, 'SELL', 52.00, 250.00, 0.00, 'OPEN', NOW());
