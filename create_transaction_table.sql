-- 创建交易账户交易记录表
CREATE TABLE `trade_account_transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `account_id` bigint NOT NULL COMMENT '账户ID',
  `type` varchar(20) NOT NULL COMMENT '交易类型：recharge(充值), withdraw(提现), freeze(冻结), unfreeze(解冻), deduct(扣除)',
  `amount` decimal(18,2) NOT NULL COMMENT '交易金额',
  `balance_before` decimal(18,2) NOT NULL COMMENT '交易前余额',
  `balance_after` decimal(18,2) NOT NULL COMMENT '交易后余额',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_account_id` (`account_id`),
  KEY `idx_type` (`type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易账户交易记录表';

-- 添加外键约束（可选）
ALTER TABLE `trade_account_transaction` 
ADD CONSTRAINT `fk_transaction_account` 
FOREIGN KEY (`account_id`) 
REFERENCES `trade_account` (`id`) 
ON DELETE CASCADE;

-- 添加外键约束到用户表（可选）
ALTER TABLE `trade_account_transaction` 
ADD CONSTRAINT `fk_transaction_user` 
FOREIGN KEY (`user_id`) 
REFERENCES `user` (`id`) 
ON DELETE CASCADE;