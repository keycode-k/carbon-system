-- ====================================
-- 审批流程表 - 数据库迁移脚本
-- 用于管理业务审批流程
-- ====================================

-- 创建审批流程定义表
CREATE TABLE IF NOT EXISTS sys_approval_flow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '流程ID',
    flow_name VARCHAR(100) NOT NULL COMMENT '流程名称',
    flow_code VARCHAR(50) NOT NULL COMMENT '流程编码',
    business_type VARCHAR(50) NOT NULL COMMENT '业务类型',
    description VARCHAR(500) COMMENT '流程描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-停用',
    create_by VARCHAR(50) COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新者',
    update_time DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_flow_code (flow_code),
    INDEX idx_business_type (business_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批流程定义表';

-- 创建审批节点表
CREATE TABLE IF NOT EXISTS sys_approval_node (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '节点ID',
    flow_id BIGINT NOT NULL COMMENT '所属流程ID',
    node_name VARCHAR(100) NOT NULL COMMENT '节点名称',
    node_order INT NOT NULL COMMENT '节点序号',
    approver_type TINYINT NOT NULL COMMENT '审批类型：1-角色审批 2-指定用户 3-部门负责人',
    approver_id BIGINT COMMENT '审批人ID/角色ID',
    approver_name VARCHAR(50) COMMENT '审批人名称',
    allow_transfer TINYINT DEFAULT 0 COMMENT '是否允许转交：1-是 0-否',
    allow_reject TINYINT DEFAULT 1 COMMENT '是否允许驳回：1-是 0-否',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_flow_id (flow_id),
    INDEX idx_node_order (node_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批流程节点表';

-- 创建审批记录表
CREATE TABLE IF NOT EXISTS sys_approval_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    flow_id BIGINT NOT NULL COMMENT '审批流程ID',
    business_type VARCHAR(50) NOT NULL COMMENT '业务类型',
    business_id BIGINT NOT NULL COMMENT '业务ID',
    business_title VARCHAR(200) COMMENT '业务标题',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    applicant_name VARCHAR(50) COMMENT '申请人名称',
    current_node_id BIGINT COMMENT '当前节点ID',
    current_node_name VARCHAR(100) COMMENT '当前节点名称',
    status TINYINT DEFAULT 0 COMMENT '审批状态：0-待审批 1-审批中 2-已通过 3-已驳回 4-已撤回',
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    complete_time DATETIME COMMENT '完成时间',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_flow_id (flow_id),
    INDEX idx_business (business_type, business_id),
    INDEX idx_applicant (applicant_id),
    INDEX idx_status (status),
    INDEX idx_apply_time (apply_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批记录表';

-- 创建审批历史表
CREATE TABLE IF NOT EXISTS sys_approval_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '历史ID',
    record_id BIGINT NOT NULL COMMENT '审批记录ID',
    node_id BIGINT COMMENT '审批节点ID',
    node_name VARCHAR(100) COMMENT '节点名称',
    approver_id BIGINT COMMENT '审批人ID',
    approver_name VARCHAR(50) COMMENT '审批人名称',
    action TINYINT NOT NULL COMMENT '审批动作：1-通过 2-驳回 3-转交',
    comment VARCHAR(500) COMMENT '审批意见',
    transfer_to_user_id BIGINT COMMENT '转交目标用户ID',
    transfer_to_user_name VARCHAR(50) COMMENT '转交目标用户名称',
    approve_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
    INDEX idx_record_id (record_id),
    INDEX idx_approver (approver_id),
    INDEX idx_approve_time (approve_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批历史表';

-- 添加字典数据 - 业务类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, remark, create_time) VALUES
('审批业务类型', 'approval_business_type', 1, '审批流程关联的业务类型', NOW())
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, status, create_time) VALUES
('approval_business_type', '资产发行', 'ASSET_ISSUE', 1, 1, NOW()),
('approval_business_type', '大额交易', 'TRADE_LARGE', 2, 1, NOW()),
('approval_business_type', '企业认证', 'COMPANY_VERIFY', 3, 1, NOW()),
('approval_business_type', '项目立项', 'PROJECT_CREATE', 4, 1, NOW()),
('approval_business_type', '减排核算', 'CARBON_CALCULATE', 5, 1, NOW())
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 添加字典数据 - 审批状态
INSERT INTO sys_dict_type (dict_name, dict_type, status, remark, create_time) VALUES
('审批状态', 'approval_status', 1, '审批记录状态', NOW())
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, status, create_time) VALUES
('approval_status', '待审批', '0', 1, 1, NOW()),
('approval_status', '审批中', '1', 2, 1, NOW()),
('approval_status', '已通过', '2', 3, 1, NOW()),
('approval_status', '已驳回', '3', 4, 1, NOW()),
('approval_status', '已撤回', '4', 5, 1, NOW())
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 添加字典数据 - 审批动作
INSERT INTO sys_dict_type (dict_name, dict_type, status, remark, create_time) VALUES
('审批动作', 'approval_action', 1, '审批操作类型', NOW())
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, status, create_time) VALUES
('approval_action', '通过', '1', 1, 1, NOW()),
('approval_action', '驳回', '2', 2, 1, NOW()),
('approval_action', '转交', '3', 3, 1, NOW())
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 插入示例审批流程
INSERT INTO sys_approval_flow (flow_name, flow_code, business_type, description, status, create_by, create_time) VALUES
('资产发行审批', 'FLOW_ASSET_ISSUE', 'ASSET_ISSUE', '碳资产发行需要经过多级审批', 1, 'admin', NOW()),
('大额交易审批', 'FLOW_TRADE_LARGE', 'TRADE_LARGE', '超过10000吨的大额交易需要审批', 1, 'admin', NOW()),
('企业认证审批', 'FLOW_COMPANY_VERIFY', 'COMPANY_VERIFY', '企业实名认证审批流程', 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE flow_name = VALUES(flow_name);

-- 插入示例审批节点（资产发行流程）
SET @flow_asset_issue = (SELECT id FROM sys_approval_flow WHERE flow_code = 'FLOW_ASSET_ISSUE' LIMIT 1);

INSERT INTO sys_approval_node (flow_id, node_name, node_order, approver_type, approver_id, approver_name, allow_transfer, allow_reject, status, create_time) VALUES
(@flow_asset_issue, '部门初审', 1, 1, 2, '审核员', 1, 1, 1, NOW()),
(@flow_asset_issue, '管理员复核', 2, 1, 1, '管理员', 0, 1, 1, NOW())
ON DUPLICATE KEY UPDATE node_name = VALUES(node_name);

-- 添加审批管理菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_path, component, menu_type, icon, order_num, permission, status, create_time)
SELECT id, '审批管理', 'approval', 'system/approval', 'C', 'Stamp', 6, 'system:approval:list', 1, NOW()
FROM sys_menu WHERE menu_path = 'system' AND parent_id = 0
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 添加审批管理按钮权限
SET @approval_menu_id = (SELECT id FROM sys_menu WHERE menu_path = 'approval' AND component = 'system/approval' LIMIT 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_path, component, menu_type, icon, order_num, permission, status, create_time) VALUES
(@approval_menu_id, '审批查询', NULL, NULL, 'F', NULL, 1, 'system:approval:query', 1, NOW()),
(@approval_menu_id, '审批新增', NULL, NULL, 'F', NULL, 2, 'system:approval:add', 1, NOW()),
(@approval_menu_id, '审批修改', NULL, NULL, 'F', NULL, 3, 'system:approval:edit', 1, NOW()),
(@approval_menu_id, '审批删除', NULL, NULL, 'F', NULL, 4, 'system:approval:delete', 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 为管理员角色分配审批管理权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE permission LIKE 'system:approval:%'
ON DUPLICATE KEY UPDATE role_id = role_id;

SELECT '审批流程表创建成功！' AS result;
