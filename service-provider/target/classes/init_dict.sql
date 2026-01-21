-- ============================================
-- 字典配置管理 - 数据库初始化脚本
-- ============================================

-- 1. 字典类型表
CREATE TABLE IF NOT EXISTS sys_dict_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '字典ID',
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称',
    dict_type VARCHAR(100) NOT NULL UNIQUE COMMENT '字典类型',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    remark VARCHAR(500) COMMENT '备注说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- 2. 字典数据表
CREATE TABLE IF NOT EXISTS sys_dict_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '字典数据ID',
    dict_type VARCHAR(100) NOT NULL COMMENT '字典类型',
    dict_label VARCHAR(100) NOT NULL COMMENT '字典标签',
    dict_value VARCHAR(100) NOT NULL COMMENT '字典键值',
    dict_sort INT DEFAULT 0 COMMENT '排序',
    css_class VARCHAR(100) COMMENT 'CSS样式类',
    list_class VARCHAR(100) COMMENT '表格回显样式',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认：0-否 1-是',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    remark VARCHAR(500) COMMENT '备注说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';


-- ============================================
-- 初始化常用字典数据
-- ============================================

-- 系统通用状态
INSERT INTO sys_dict_type (dict_name, dict_type, remark) VALUES
('系统状态', 'sys_status', '系统通用状态：启用/禁用'),
('是否', 'sys_yes_no', '是否选项'),
('显示状态', 'sys_show_hide', '显示/隐藏状态')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, list_class) VALUES
('sys_status', '启用', '1', 1, 'success'),
('sys_status', '禁用', '0', 2, 'danger'),
('sys_yes_no', '是', '1', 1, 'primary'),
('sys_yes_no', '否', '0', 2, 'info'),
('sys_show_hide', '显示', '1', 1, 'primary'),
('sys_show_hide', '隐藏', '0', 2, 'info')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);


-- 资产类型
INSERT INTO sys_dict_type (dict_name, dict_type, remark) VALUES
('资产类型', 'asset_type', '碳资产类型：配额/信用')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, list_class) VALUES
('asset_type', '碳配额', 'QUOTA', 1, 'primary'),
('asset_type', '碳信用(CCER)', 'CREDIT', 2, 'success')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);


-- 交易订单状态
INSERT INTO sys_dict_type (dict_name, dict_type, remark) VALUES
('订单状态', 'order_status', '交易订单状态')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, list_class) VALUES
('order_status', '待成交', 'OPEN', 1, 'warning'),
('order_status', '部分成交', 'PARTIAL', 2, 'primary'),
('order_status', '已成交', 'CLOSED', 3, 'success'),
('order_status', '已取消', 'CANCELLED', 4, 'info'),
('order_status', '已过期', 'EXPIRED', 5, 'danger')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);


-- 交易方向
INSERT INTO sys_dict_type (dict_name, dict_type, remark) VALUES
('交易方向', 'trade_direction', '买入/卖出')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, list_class) VALUES
('trade_direction', '买入', 'BUY', 1, 'danger'),
('trade_direction', '卖出', 'SELL', 2, 'success')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);


-- 企业认证状态
INSERT INTO sys_dict_type (dict_name, dict_type, remark) VALUES
('认证状态', 'certification_status', '企业认证状态')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, list_class) VALUES
('certification_status', '未认证', 'UNCERTIFIED', 1, 'info'),
('certification_status', '审核中', 'PENDING', 2, 'warning'),
('certification_status', '已认证', 'CERTIFIED', 3, 'success'),
('certification_status', '已驳回', 'REJECTED', 4, 'danger')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);


-- 项目开发阶段
INSERT INTO sys_dict_type (dict_name, dict_type, remark) VALUES
('项目阶段', 'project_stage', '碳项目开发阶段')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, list_class) VALUES
('project_stage', '立项', 'INITIATION', 1, 'info'),
('project_stage', 'PDD设计', 'PDD_DESIGN', 2, 'primary'),
('project_stage', '审定', 'VALIDATION', 3, 'primary'),
('project_stage', '备案', 'REGISTRATION', 4, 'warning'),
('project_stage', '监测', 'MONITORING', 5, 'warning'),
('project_stage', '核证', 'VERIFICATION', 6, 'warning'),
('project_stage', '签发', 'ISSUANCE', 7, 'success'),
('project_stage', '完成', 'COMPLETED', 8, 'success')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);


-- 文档状态
INSERT INTO sys_dict_type (dict_name, dict_type, remark) VALUES
('文档状态', 'document_status', '项目文档审核状态')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, list_class) VALUES
('document_status', '草稿', 'DRAFT', 1, 'info'),
('document_status', '审核中', 'REVIEWING', 2, 'warning'),
('document_status', '已批准', 'APPROVED', 3, 'success'),
('document_status', '已驳回', 'REJECTED', 4, 'danger')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);


-- 询报价状态
INSERT INTO sys_dict_type (dict_name, dict_type, remark) VALUES
('询价状态', 'quote_status', '询报价单状态')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, list_class) VALUES
('quote_status', '待报价', 'PENDING', 1, 'warning'),
('quote_status', '已报价', 'QUOTED', 2, 'primary'),
('quote_status', '已接受', 'ACCEPTED', 3, 'success'),
('quote_status', '已拒绝', 'REJECTED', 4, 'danger'),
('quote_status', '已过期', 'EXPIRED', 5, 'info')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);


-- 企业类型
INSERT INTO sys_dict_type (dict_name, dict_type, remark) VALUES
('企业类型', 'company_type', '企业行业类型')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, list_class) VALUES
('company_type', '电力企业', 'POWER', 1, ''),
('company_type', '钢铁企业', 'STEEL', 2, ''),
('company_type', '化工企业', 'CHEMICAL', 3, ''),
('company_type', '水泥企业', 'CEMENT', 4, ''),
('company_type', '航空企业', 'AVIATION', 5, ''),
('company_type', '其他重点排放单位', 'OTHER', 6, '')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);


-- 菜单类型
INSERT INTO sys_dict_type (dict_name, dict_type, remark) VALUES
('菜单类型', 'menu_type', '系统菜单类型')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, list_class) VALUES
('menu_type', '目录', 'M', 1, 'primary'),
('menu_type', '菜单', 'C', 2, 'success'),
('menu_type', '按钮', 'F', 3, 'warning')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);
