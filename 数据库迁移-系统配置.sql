-- ====================================
-- 系统配置表 - 数据库迁移脚本
-- 用于存储系统参数配置
-- ====================================

-- 创建系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    config_name VARCHAR(100) NOT NULL COMMENT '配置名称',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键名',
    config_value VARCHAR(2000) COMMENT '配置值',
    config_type TINYINT DEFAULT 2 COMMENT '配置类型：1-系统内置 2-自定义',
    config_group VARCHAR(50) DEFAULT 'default' COMMENT '配置分组',
    remark VARCHAR(500) COMMENT '备注',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-停用',
    create_by VARCHAR(50) COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新者',
    update_time DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_config_key (config_key),
    INDEX idx_config_group (config_group),
    INDEX idx_config_type (config_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 插入初始系统配置
INSERT INTO sys_config (config_name, config_key, config_value, config_type, config_group, remark, status, create_time) VALUES
-- 系统基础配置
('系统名称', 'sys.name', '碳中和数字碳资产管理系统', 1, 'system', '系统显示名称', 1, NOW()),
('系统版本', 'sys.version', '1.0.0', 1, 'system', '当前系统版本号', 1, NOW()),
('系统Logo', 'sys.logo', '/logo.png', 1, 'system', '系统Logo图片路径', 1, NOW()),
('版权信息', 'sys.copyright', 'Copyright © 2025 Carbon System', 1, 'system', '页脚版权信息', 1, NOW()),

-- 账户安全配置
('密码最小长度', 'account.password.minLength', '6', 1, 'account', '用户密码最小长度', 1, NOW()),
('密码最大错误次数', 'account.password.maxRetry', '5', 1, 'account', '密码错误最大尝试次数，超过后锁定账户', 1, NOW()),
('账户锁定时间(分钟)', 'account.lockTime', '10', 1, 'account', '账户锁定时间，单位分钟', 1, NOW()),
('允许注册', 'account.register.enabled', 'true', 1, 'account', '是否允许用户自主注册', 1, NOW()),
('默认用户角色', 'account.default.roleId', '3', 1, 'account', '新注册用户的默认角色ID', 1, NOW()),

-- 交易配置
('交易开始时间', 'trade.time.start', '09:30', 1, 'trade', '每日交易开始时间', 1, NOW()),
('交易结束时间', 'trade.time.end', '15:00', 1, 'trade', '每日交易结束时间', 1, NOW()),
('最小交易数量', 'trade.min.quantity', '100', 1, 'trade', '单笔交易最小数量', 1, NOW()),
('最大交易数量', 'trade.max.quantity', '100000', 1, 'trade', '单笔交易最大数量', 1, NOW()),
('交易手续费率(%)', 'trade.fee.rate', '0.1', 1, 'trade', '交易手续费率，百分比', 1, NOW()),

-- 上传配置
('最大文件大小(MB)', 'upload.maxSize', '10', 1, 'upload', '文件上传最大大小，单位MB', 1, NOW()),
('允许的文件类型', 'upload.allowedTypes', 'jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx', 1, 'upload', '允许上传的文件扩展名', 1, NOW()),
('上传路径', 'upload.path', '/uploads', 1, 'upload', '文件上传存储路径', 1, NOW()),

-- 短信/邮件配置
('短信验证码有效期(分钟)', 'sms.code.expire', '5', 1, 'notification', '短信验证码有效期', 1, NOW()),
('邮件验证码有效期(分钟)', 'email.code.expire', '15', 1, 'notification', '邮件验证码有效期', 1, NOW()),

-- 日志配置
('操作日志保留天数', 'log.operation.retainDays', '90', 1, 'log', '操作日志保留天数', 1, NOW()),
('登录日志保留天数', 'log.login.retainDays', '180', 1, 'log', '登录日志保留天数', 1, NOW())
ON DUPLICATE KEY UPDATE config_name = VALUES(config_name);

-- 添加配置管理相关菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_path, component, menu_type, icon, order_num, permission, status, create_time)
SELECT id, '参数配置', 'config', 'system/config', 'C', 'Setting', 5, 'system:config:list', 1, NOW()
FROM sys_menu WHERE menu_path = 'system' AND parent_id = 0
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 添加配置管理按钮权限
SET @config_menu_id = (SELECT id FROM sys_menu WHERE menu_path = 'config' AND component = 'system/config' LIMIT 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_path, component, menu_type, icon, order_num, permission, status, create_time) VALUES
(@config_menu_id, '配置查询', NULL, NULL, 'F', NULL, 1, 'system:config:query', 1, NOW()),
(@config_menu_id, '配置新增', NULL, NULL, 'F', NULL, 2, 'system:config:add', 1, NOW()),
(@config_menu_id, '配置修改', NULL, NULL, 'F', NULL, 3, 'system:config:edit', 1, NOW()),
(@config_menu_id, '配置删除', NULL, NULL, 'F', NULL, 4, 'system:config:delete', 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 为管理员角色分配配置管理权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE permission LIKE 'system:config:%'
ON DUPLICATE KEY UPDATE role_id = role_id;

SELECT '系统配置表创建成功！' AS result;
