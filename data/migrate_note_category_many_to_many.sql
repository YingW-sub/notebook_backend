-- =============================================
-- 笔记与分类多对多关联重构
-- note_tables.sql 未写 CONSTRAINT 名时，MySQL 常自动生成 note_ibfk_1。
-- 若下面 DROP 报错，执行：
--   SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
--   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'note' AND COLUMN_NAME = 'category_id'
--     AND REFERENCED_TABLE_NAME IS NOT NULL;
-- 将下一行中的名字改为查询结果。
-- =============================================
SET FOREIGN_KEY_CHECKS = 0;

-- 步骤2：删除 note 表的 category_id 外键（MySQL 通常命名为 note_ibfk_1）
ALTER TABLE note DROP FOREIGN KEY note_ibfk_1;

-- 步骤3：先删索引，再删列，顺序不能乱
ALTER TABLE note DROP INDEX idx_category_id;
ALTER TABLE note DROP COLUMN category_id;

-- 步骤4：创建多对多关联表
DROP TABLE IF EXISTS note_category_relation;

-- 一条笔记可属于多个分类，一个分类可包含多条笔记
CREATE TABLE note_category_relation (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    note_id         BIGINT          NOT NULL COMMENT '笔记ID',
    category_id     BIGINT          NOT NULL COMMENT '分类ID',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    -- note_id + category_id 唯一索引，防止同一笔记重复关联同一分类
    UNIQUE INDEX uk_note_category (note_id, category_id),
    INDEX idx_note_id (note_id),      -- 按笔记查关联
    INDEX idx_category_id (category_id), -- 按分类查关联
    -- 级联删除：笔记或分类被删除时，自动清理关联记录
    FOREIGN KEY (note_id) REFERENCES note(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记与分类多对多关联表';

SET FOREIGN_KEY_CHECKS = 1;

