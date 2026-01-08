-- ============================================================
-- JWT认证和用户管理增强SQL迁移脚本
-- 执行时间：2024-01
-- 功能：为user表添加status字段，支持用户启用禁用功能
-- ============================================================

-- 1. 为user表添加status字段
-- 状态：0-禁用 1-启用
-- 注意：如果字段已存在会报错，可以忽略该错误继续执行后续语句
ALTER TABLE `user` ADD COLUMN `status` TINYINT(1) DEFAULT 1 COMMENT '用户状态：0-禁用 1-启用';

-- 2. 更新现有用户状态为启用
UPDATE `user` SET `status` = 1 WHERE `status` IS NULL;

-- 3. 创建索引优化查询（如果不存在）
-- 注意：MySQL 5.7不支持IF NOT EXISTS语法的索引创建，可能需要手动检查
-- CREATE INDEX IF NOT EXISTS idx_user_status ON `user` (`status`);
-- CREATE INDEX IF NOT EXISTS idx_user_username ON `user` (`username`);

-- 4. 创建jwt_blacklist表用于存储已注销的token（可选，用于实现token黑名单机制）
CREATE TABLE IF NOT EXISTS `jwt_blacklist` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `token` VARCHAR(500) NOT NULL COMMENT 'JWT Token',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_token` (`token`(255)),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='JWT Token黑名单表';

-- 5. 更新RBAC权限数据，添加用户管理相关权限
-- 注意：sys_menu表字段为 menu_path, permission（不是path, perms）
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `menu_path`, `component`, `menu_type`, `permission`, `icon`, `status`, `create_time`, `update_time`) VALUES
('用户管理', (SELECT id FROM (SELECT id FROM sys_menu WHERE menu_name = '系统管理' LIMIT 1) AS temp), 1, '/system/user', 'system/user', 'C', 'system:user:list', 'User', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 获取用户管理菜单的ID
SET @user_menu_id = (SELECT id FROM sys_menu WHERE permission = 'system:user:list' LIMIT 1);

-- 添加用户管理的按钮权限
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `menu_path`, `component`, `menu_type`, `permission`, `icon`, `status`, `create_time`, `update_time`) VALUES
('用户查询', @user_menu_id, 1, '', NULL, 'F', 'system:user:query', '#', 1, NOW(), NOW()),
('用户新增', @user_menu_id, 2, '', NULL, 'F', 'system:user:add', '#', 1, NOW(), NOW()),
('用户修改', @user_menu_id, 3, '', NULL, 'F', 'system:user:edit', '#', 1, NOW(), NOW()),
('用户删除', @user_menu_id, 4, '', NULL, 'F', 'system:user:delete', '#', 1, NOW(), NOW()),
('重置密码', @user_menu_id, 5, '', NULL, 'F', 'system:user:resetPwd', '#', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 6. 为管理员角色分配用户管理权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.id, m.id FROM sys_role r, sys_menu m 
WHERE r.role_code = 'admin' 
AND m.permission IN ('system:user:list', 'system:user:query', 'system:user:add', 'system:user:edit', 'system:user:delete', 'system:user:resetPwd')
ON DUPLICATE KEY UPDATE role_id = role_id;

-- ============================================================
-- 执行完成
-- ============================================================
