-- ====================================
-- 操作日志表 - 数据库迁移脚本
-- 用于记录用户操作日志
-- ====================================

-- 创建操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    module VARCHAR(50) COMMENT '操作模块',
    operation_type VARCHAR(20) COMMENT '操作类型：CREATE/UPDATE/DELETE/QUERY/LOGIN/LOGOUT/EXPORT/IMPORT/OTHER',
    description VARCHAR(255) COMMENT '操作描述',
    method VARCHAR(255) COMMENT '请求方法（类名.方法名）',
    request_url VARCHAR(255) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    ip VARCHAR(50) COMMENT '操作IP地址',
    location VARCHAR(100) COMMENT '操作地点',
    browser VARCHAR(100) COMMENT '浏览器',
    os VARCHAR(100) COMMENT '操作系统',
    status TINYINT DEFAULT 1 COMMENT '操作状态：1-成功，0-失败',
    error_msg TEXT COMMENT '错误信息',
    duration BIGINT COMMENT '执行时长(毫秒)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_username (username),
    INDEX idx_module (module),
    INDEX idx_operation_type (operation_type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作日志表';

-- 添加字典数据 - 操作类型
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, status, create_time) VALUES
('operation_type', '新增', 'CREATE', 1, 1, NOW()),
('operation_type', '修改', 'UPDATE', 2, 1, NOW()),
('operation_type', '删除', 'DELETE', 3, 1, NOW()),
('operation_type', '查询', 'QUERY', 4, 1, NOW()),
('operation_type', '登录', 'LOGIN', 5, 1, NOW()),
('operation_type', '登出', 'LOGOUT', 6, 1, NOW()),
('operation_type', '导出', 'EXPORT', 7, 1, NOW()),
('operation_type', '导入', 'IMPORT', 8, 1, NOW()),
('operation_type', '其他', 'OTHER', 9, 1, NOW())
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- 添加字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, remark, create_time) VALUES
('操作类型', 'operation_type', 1, '系统操作日志类型', NOW())
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 添加操作日志相关菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_path, component, menu_type, icon, order_num, permission, status, create_time) VALUES
(0, '日志管理', 'log', NULL, 'M', 'Document', 90, NULL, 1, NOW());

SET @log_menu_id = LAST_INSERT_ID();

INSERT INTO sys_menu (parent_id, menu_name, menu_path, component, menu_type, icon, order_num, permission, status, create_time) VALUES
(@log_menu_id, '操作日志', 'operation', 'system/operation', 'C', 'List', 1, 'system:log:list', 1, NOW());

SET @operation_log_menu_id = LAST_INSERT_ID();

INSERT INTO sys_menu (parent_id, menu_name, menu_path, component, menu_type, icon, order_num, permission, status, create_time) VALUES
(@operation_log_menu_id, '日志查询', NULL, NULL, 'F', NULL, 1, 'system:log:query', 1, NOW()),
(@operation_log_menu_id, '日志删除', NULL, NULL, 'F', NULL, 2, 'system:log:delete', 1, NOW());

-- 为管理员角色分配日志管理权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE permission IN ('system:log:list', 'system:log:query', 'system:log:delete')
ON DUPLICATE KEY UPDATE role_id = role_id;

SELECT '操作日志表创建成功！' AS result;
