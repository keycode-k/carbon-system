-- =====================================================
-- 碳资产开发模块数据库迁移脚本
-- 执行时间: 2026-01-08
-- 功能: 扩展开发项目表字段，完善项目管理功能
-- =====================================================

-- 1. 为 development_project 表添加新字段（使用存储过程兼容MySQL）
DROP PROCEDURE IF EXISTS add_column_if_not_exists;

DELIMITER //
CREATE PROCEDURE add_column_if_not_exists()
BEGIN
    -- 添加 project_type 字段
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = DATABASE() 
                   AND table_name = 'development_project' 
                   AND column_name = 'project_type') THEN
        ALTER TABLE `development_project` ADD COLUMN `project_type` VARCHAR(50) DEFAULT NULL COMMENT '项目类型(风电/光伏/林业碳汇等)' AFTER `name`;
    END IF;
    
    -- 添加 methodology_id 字段
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = DATABASE() 
                   AND table_name = 'development_project' 
                   AND column_name = 'methodology_id') THEN
        ALTER TABLE `development_project` ADD COLUMN `methodology_id` BIGINT(20) DEFAULT NULL COMMENT '方法学ID' AFTER `project_type`;
    END IF;
    
    -- 添加 current_step 字段
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = DATABASE() 
                   AND table_name = 'development_project' 
                   AND column_name = 'current_step') THEN
        ALTER TABLE `development_project` ADD COLUMN `current_step` INT DEFAULT 0 COMMENT '当前阶段(0-5)' AFTER `status`;
    END IF;
    
    -- 添加 description 字段
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = DATABASE() 
                   AND table_name = 'development_project' 
                   AND column_name = 'description') THEN
        ALTER TABLE `development_project` ADD COLUMN `description` TEXT COMMENT '项目描述' AFTER `estimated_emission_reduction`;
    END IF;
END //
DELIMITER ;

CALL add_column_if_not_exists();
DROP PROCEDURE IF EXISTS add_column_if_not_exists;

-- 2. 创建索引（忽略已存在的错误）
-- 如果索引已存在会报错，可以忽略
-- CREATE INDEX idx_methodology_id ON development_project(methodology_id);
-- CREATE INDEX idx_status ON development_project(status);

-- 3. 将旧的 methodology 字段数据迁移到 methodology_id
-- 首先查找匹配的方法学ID
UPDATE development_project dp
LEFT JOIN methodology m ON dp.methodology = m.code
SET dp.methodology_id = m.id
WHERE dp.methodology IS NOT NULL AND dp.methodology_id IS NULL;

-- 4. 删除旧的 methodology 字段（可选，建议先备份数据）
-- ALTER TABLE `development_project` DROP COLUMN IF EXISTS `methodology`;

-- 5. 插入更丰富的测试项目数据
INSERT INTO `development_project` (`name`, `project_type`, `methodology_id`, `status`, `current_step`, `owner_id`, `location`, `estimated_emission_reduction`, `description`)
SELECT '内蒙古100MW风电并网项目', '风电', m.id, 'PLANNED', 1, 1, '内蒙古锡林郭勒盟', 150000.0, '利用内蒙古丰富的风力资源，建设100MW风力发电场，预计年发电量2.5亿kWh'
FROM `methodology` m WHERE m.code = 'CM-012-V01' AND NOT EXISTS (SELECT 1 FROM development_project WHERE name = '内蒙古100MW风电并网项目')
LIMIT 1;

INSERT INTO `development_project` (`name`, `project_type`, `methodology_id`, `status`, `current_step`, `owner_id`, `location`, `estimated_emission_reduction`, `description`)
SELECT '广东某造纸厂生物质锅炉改造', '节能提效', m.id, 'REGISTERED', 3, 2, '广东省东莞市', 35000.0, '将传统燃煤锅炉改造为生物质燃料锅炉，减少碳排放'
FROM `methodology` m WHERE m.code = 'CM-021-V01' AND NOT EXISTS (SELECT 1 FROM development_project WHERE name = '广东某造纸厂生物质锅炉改造')
LIMIT 1;

INSERT INTO `development_project` (`name`, `project_type`, `methodology_id`, `status`, `current_step`, `owner_id`, `location`, `estimated_emission_reduction`, `description`)
SELECT '福建沿海红树林修复工程', '林业碳汇', m.id, 'PLANNED', 2, 3, '福建省漳州市', 28000.0, '修复和保护福建沿海红树林生态系统，增加蓝碳储量'
FROM `methodology` m WHERE m.code = 'CM-001-V01' AND NOT EXISTS (SELECT 1 FROM development_project WHERE name = '福建沿海红树林修复工程')
LIMIT 1;

INSERT INTO `development_project` (`name`, `project_type`, `methodology_id`, `status`, `current_step`, `owner_id`, `location`, `estimated_emission_reduction`, `description`)
SELECT '浙江50MW光伏发电项目', '光伏', m.id, 'VERIFIED', 4, 1, '浙江省杭州市', 42000.0, '利用厂房屋顶建设分布式光伏发电系统'
FROM `methodology` m WHERE m.code = 'CM-011-V01' AND NOT EXISTS (SELECT 1 FROM development_project WHERE name = '浙江50MW光伏发电项目')
LIMIT 1;

INSERT INTO `development_project` (`name`, `project_type`, `methodology_id`, `status`, `current_step`, `owner_id`, `location`, `estimated_emission_reduction`, `description`)
SELECT '上海垃圾焚烧发电项目', '废弃物处理', m.id, 'ISSUED', 5, 2, '上海市嘉定区', 85000.0, '城市生活垃圾焚烧发电项目，日处理能力2000吨'
FROM `methodology` m WHERE m.code = 'CM-031-V01' AND NOT EXISTS (SELECT 1 FROM development_project WHERE name = '上海垃圾焚烧发电项目')
LIMIT 1;

-- =====================================================
-- 迁移完成提示
-- =====================================================
SELECT '碳资产开发模块数据库迁移完成!' AS message;
