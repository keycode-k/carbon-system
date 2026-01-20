﻿﻿﻿﻿﻿﻿﻿﻿﻿-- 创建碳配额表
CREATE TABLE IF NOT EXISTS `carbon_quota` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '所属用户ID',
  `year` INT(4) NOT NULL COMMENT '履约年度',
  `total_quota` DECIMAL(20, 2) DEFAULT 0.00 COMMENT '发放总配额',
  `verified_emission` DECIMAL(20, 2) DEFAULT 0.00 COMMENT '核查排放量',
  `status` TINYINT(1) DEFAULT 0 COMMENT '履约状态 0-未履约 1-已履约',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_year` (`user_id`, `year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='碳配额表';

-- 插入一些测试数据 (假设用户ID为1)
INSERT IGNORE INTO `carbon_quota` (`user_id`, `year`, `total_quota`, `verified_emission`, `status`) 
VALUES (1, 2025, 1000000.00, 950000.00, 1);

-- 创建碳信用(CCER)表
CREATE TABLE IF NOT EXISTS `carbon_credit` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '所属用户ID',
  `project_name` VARCHAR(255) DEFAULT NULL COMMENT '项目名称',
  `project_type` VARCHAR(50) DEFAULT NULL COMMENT '项目类型(如: 风电, 光伏, 林业)',
  `amount` DECIMAL(20, 2) DEFAULT 0.00 COMMENT '持有数量(tCO2e)',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态 0-持有中 1-交易中 2-已注销',
  `issue_date` DATE DEFAULT NULL COMMENT '签发日期',
  `expiry_date` DATE DEFAULT NULL COMMENT '有效期',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='碳信用表';

-- 插入一些CCER测试数据（使用 INSERT IGNORE 避免重复插入错误）
INSERT IGNORE INTO `carbon_credit` (`id`, `user_id`, `project_name`, `project_type`, `amount`, `status`, `issue_date`, `expiry_date`)
VALUES (1, 1, '河北张家口风电项目', '风电', 5000.00, 0, '2024-05-20', '2029-05-20');
INSERT IGNORE INTO `carbon_credit` (`id`, `user_id`, `project_name`, `project_type`, `amount`, `status`, `issue_date`, `expiry_date`)
VALUES (2, 1, '四川广元造林碳汇项目', '林业', 2000.00, 0, '2023-08-15', '2028-08-15');
INSERT IGNORE INTO `carbon_credit` (`id`, `user_id`, `project_name`, `project_type`, `amount`, `status`, `issue_date`, `expiry_date`)
VALUES (3, 1, '云南大理光伏发电项目', '光伏', 3500.00, 0, '2024-01-10', '2029-01-10');

-- 创建配额明细表
CREATE TABLE IF NOT EXISTS `carbon_quota_detail` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `quota_id` BIGINT(20) NOT NULL COMMENT '关联配额ID',
  `type` VARCHAR(50) COMMENT '变动类型',
  `amount` DECIMAL(20, 2) COMMENT '变动数量',
  `balance` DECIMAL(20, 2) COMMENT '变动后结余',
  `remark` VARCHAR(255) COMMENT '备注',
  `change_date` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '变动时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配额明细表';

-- 插入测试明细
INSERT IGNORE INTO `carbon_quota_detail` (`id`, `quota_id`, `type`, `amount`, `balance`, `remark`, `change_date`)
VALUES (1, 1, '初始发放', 1000000.00, 1000000.00, '2025年度配额预发放', NOW());
