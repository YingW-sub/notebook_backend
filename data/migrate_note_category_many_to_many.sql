-- =============================================
-- 笔记与分类多对多关联重构 - 数据库迁移脚本
-- 执行时间: 2026-03-22
--
-- 兼容说明：
--   DROP FOREIGN KEY IF EXISTS / DROP COLUMN IF EXISTS 等写法需要 MySQL 8.0.29+。
--   本脚本改为标准写法，适用于 MySQL 8.0.0 ~ 8.0.28 及更高版本。
--
-- note_tables.sql 未写 CONSTRAINT 名时，MySQL 常自动生成 note_ibfk_1。
-- 若下面 DROP 报错，执行：
--   SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
--   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'note' AND COLUMN_NAME = 'category_id'
--     AND REFERENCED_TABLE_NAME IS NOT NULL;
-- 将下一行中的名字改为查询结果。
-- =============================================
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------
-- 步骤2：删除 note 与 category 之间的外键（无 IF EXISTS，兼容旧版 8.0）
-- -----------------------------------------------------
ALTER TABLE note DROP FOREIGN KEY note_ibfk_1;

-- -----------------------------------------------------
-- 步骤3：删除 category_id 上的索引与列（拆成多条，避免 IF EXISTS 语法）
-- -----------------------------------------------------
ALTER TABLE note DROP INDEX idx_category_id;

ALTER TABLE note DROP COLUMN category_id;

-- -----------------------------------------------------
-- 步骤4：创建关联表 note_category_relation
-- -----------------------------------------------------
DROP TABLE IF EXISTS note_category_relation;

CREATE TABLE note_category_relation (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    note_id         BIGINT          NOT NULL COMMENT '笔记ID',
    category_id     BIGINT          NOT NULL COMMENT '分类ID',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_note_category (note_id, category_id),
    INDEX idx_note_id (note_id),
    INDEX idx_category_id (category_id),
    FOREIGN KEY (note_id) REFERENCES note(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记与分类多对多关联表';

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 迁移完成验证（可选执行）
-- =============================================
-- SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS
--     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'note' AND COLUMN_NAME = 'category_id';
-- SELECT COUNT(*) FROM note_category_relation;
