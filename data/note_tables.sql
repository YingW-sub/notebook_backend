-- =============================================
-- 笔记本功能数据库表结构
-- 创建时间: 2026-03-17
-- =============================================
SET FOREIGN_KEY_CHECKS=0;
-- -----------------------------------------------------
-- 表1: category (分类表)
-- 用于管理用户的知识分类体系
-- -----------------------------------------------------
DROP TABLE IF EXISTS category;
CREATE TABLE category (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id         BIGINT          NOT NULL COMMENT '用户ID（数据隔离）',
    category_name   VARCHAR(100)    NOT NULL COMMENT '分类名称',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '删除标记（0-未删除，1-已删除）',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_user_deleted (user_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- -----------------------------------------------------
-- 表2: note (笔记表)
-- 存储用户的笔记内容
-- -----------------------------------------------------
DROP TABLE IF EXISTS note;
CREATE TABLE note (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id         BIGINT          NOT NULL COMMENT '用户ID（数据隔离）',
    category_id    BIGINT          NULL COMMENT '分类ID',
    title           VARCHAR(200)    NOT NULL COMMENT '笔记标题',
    content         TEXT            NULL COMMENT '笔记正文内容',
    summary         VARCHAR(500)    NULL COMMENT 'AI提取的内容摘要',
    is_starred      TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否星标收藏（0-否，1-是）',
    pinned          TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否置顶（0-否，1-是）',
    sort_order      INT             NOT NULL DEFAULT 0 COMMENT '同用户内排序，越小越靠前',
    deleted         TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '删除标记（0-未删除，1-已删除-进入回收站）',
    delete_time     DATETIME        NULL COMMENT '删除时间（用于15天自动清理）',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_user_starred (user_id, is_starred),
    INDEX idx_user_deleted (user_id, deleted),
    INDEX idx_delete_time (delete_time),
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记表';

-- -----------------------------------------------------
-- 表3: chat_message (AI聊天记录表)
-- 存储用户与AI助手的连续对话记录
-- -----------------------------------------------------
DROP TABLE IF EXISTS chat_message;
CREATE TABLE chat_message (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id         BIGINT          NOT NULL COMMENT '用户ID（数据隔离）',
    session_id      VARCHAR(64)     NOT NULL COMMENT '会话ID（用于连续上下文）',
    role            VARCHAR(20)     NOT NULL COMMENT '角色（user-用户，assistant-AI）',
    message_content TEXT            NOT NULL COMMENT '消息内容',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_user_session (user_id, session_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天记录表';