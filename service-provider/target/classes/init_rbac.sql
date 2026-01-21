-- ============================================
-- RBAC 角色权限管理系统 - 数据库初始化脚本
-- ============================================

-- 1. 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_role_code (role_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- 2. 菜单权限表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID，顶级为0',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_path VARCHAR(200) COMMENT '路由路径',
    component VARCHAR(200) COMMENT '组件路径',
    permission VARCHAR(100) COMMENT '权限标识',
    menu_type CHAR(1) NOT NULL COMMENT '菜单类型：M-目录 C-菜单 F-按钮',
    icon VARCHAR(100) COMMENT '菜单图标',
    order_num INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    visible TINYINT DEFAULT 1 COMMENT '是否显示：0-隐藏 1-显示',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单权限表';

-- 3. 角色-菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id),
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 4. 用户-角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';


-- ============================================
-- 初始化数据
-- ============================================

-- 插入默认角色
INSERT INTO sys_role (role_name, role_code, description, status) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有所有权限的超级管理员', 1),
('系统管理员', 'ADMIN', '系统管理员，管理用户和基础配置', 1),
('普通用户', 'USER', '普通用户，基础功能权限', 1),
('企业用户', 'ENTERPRISE', '企业用户，可进行碳交易', 1),
('审核员', 'AUDITOR', '审核员，负责审核企业认证和交易', 1)
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

-- 插入系统菜单
-- 顶级目录
INSERT INTO sys_menu (id, parent_id, menu_name, menu_path, component, menu_type, icon, order_num) VALUES
(1, 0, '首页', '/dashboard', 'dashboard/index', 'C', 'HomeFilled', 1),
(2, 0, '资产管理', '/assets', '', 'M', 'Wallet', 2),
(3, 0, '交易中心', '/market', '', 'M', 'TrendCharts', 3),
(4, 0, '开发工作台', '/development', '', 'M', 'DataBoard', 4),
(5, 0, '碳资讯', '/news', '', 'M', 'Document', 5),
(6, 0, '账户管理', '/account', '', 'M', 'User', 6),
(7, 0, '系统管理', '/system', '', 'M', 'Setting', 7)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 资产管理子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_path, component, permission, menu_type, icon, order_num) VALUES
(21, 2, '碳配额', '/assets/quota', 'assets/quota', 'assets:quota:view', 'C', 'Coin', 1),
(22, 2, '碳信用', '/assets/credit', 'assets/credit', 'assets:credit:view', 'C', 'Medal', 2)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 交易中心子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_path, component, permission, menu_type, icon, order_num) VALUES
(31, 3, '交易账户', '/market/account', 'market/account', 'trade:account:view', 'C', 'CreditCard', 1),
(32, 3, '交易大厅', '/market/exchange', 'market/exchange', 'trade:exchange:view', 'C', 'Shop', 2),
(33, 3, '询报价', '/market/quotes', 'market/quotes', 'trade:quotes:view', 'C', 'Tickets', 3),
(34, 3, '供需信息', '/market/supply-demand', 'market/supply-demand', 'trade:supply:view', 'C', 'Connection', 4),
(35, 3, '交易记录', '/market/trade-records', 'market/trade-records', 'trade:records:view', 'C', 'List', 5)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 开发工作台子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_path, component, permission, menu_type, icon, order_num) VALUES
(41, 4, '工作台', '/development/workbench', 'development/workbench', 'dev:workbench:view', 'C', 'Platform', 1),
(42, 4, '项目管理', '/development/project', 'development/project', 'dev:project:view', 'C', 'Folder', 2),
(43, 4, '方法学库', '/development/methodology', 'development/methodology', 'dev:methodology:view', 'C', 'Collection', 3)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 碳资讯子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_path, component, permission, menu_type, icon, order_num) VALUES
(51, 5, '政策法规', '/news/policy', 'news/policy', 'news:policy:view', 'C', 'Memo', 1),
(52, 5, '市场动态', '/news/market', 'news/market', 'news:market:view', 'C', 'Histogram', 2)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 账户管理子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_path, component, permission, menu_type, icon, order_num) VALUES
(61, 6, '企业信息', '/account/company', 'account/company', 'account:company:view', 'C', 'OfficeBuilding', 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 系统管理子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_path, component, permission, menu_type, icon, order_num) VALUES
(71, 7, '用户管理', '/system/user', 'system/user', 'system:user:view', 'C', 'UserFilled', 1),
(72, 7, '角色管理', '/system/role', 'system/role', 'system:role:view', 'C', 'Avatar', 2),
(73, 7, '菜单管理', '/system/menu', 'system/menu', 'system:menu:view', 'C', 'Menu', 3),
(74, 7, '字典管理', '/system/dict', 'system/dict', 'system:dict:view', 'C', 'Reading', 4)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, permission, menu_type, order_num) VALUES
-- 用户管理按钮
(711, 71, '新增用户', 'system:user:add', 'F', 1),
(712, 71, '编辑用户', 'system:user:edit', 'F', 2),
(713, 71, '删除用户', 'system:user:delete', 'F', 3),
-- 角色管理按钮
(721, 72, '新增角色', 'system:role:add', 'F', 1),
(722, 72, '编辑角色', 'system:role:edit', 'F', 2),
(723, 72, '删除角色', 'system:role:delete', 'F', 3),
(724, 72, '分配权限', 'system:role:assign', 'F', 4),
-- 交易相关按钮
(311, 32, '发布订单', 'trade:order:publish', 'F', 1),
(312, 32, '撤销订单', 'trade:order:cancel', 'F', 2)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 为超级管理员分配所有菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 为系统管理员分配系统管理权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1), (2, 7), (2, 71), (2, 72), (2, 73), (2, 74),
(2, 711), (2, 712), (2, 713), (2, 721), (2, 722), (2, 723), (2, 724)
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 为普通用户分配基础权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1), (3, 2), (3, 21), (3, 22), (3, 5), (3, 51), (3, 52), (3, 6), (3, 61)
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 为企业用户分配交易相关权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 1), (4, 2), (4, 21), (4, 22), (4, 3), (4, 31), (4, 32), (4, 33), (4, 34), (4, 35),
(4, 4), (4, 41), (4, 42), (4, 43), (4, 5), (4, 51), (4, 52), (4, 6), (4, 61),
(4, 311), (4, 312)
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 为用户ID=1的admin分配超级管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1)
ON DUPLICATE KEY UPDATE role_id = role_id;
