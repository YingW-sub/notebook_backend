-- 站内通知（好友解除、协作审核结果等）
CREATE TABLE IF NOT EXISTS user_notification (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         INT             NOT NULL COMMENT '接收人 admin.id',
    category        VARCHAR(40)     NOT NULL COMMENT 'FRIEND_REMOVED_PEER / FRIEND_REMOVED_ADMIN / SHARE_APPROVED / SHARE_REJECTED',
    title           VARCHAR(200)    NOT NULL,
    body            VARCHAR(1000)   NULL,
    read_flag       TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_un_user_read (user_id, read_flag),
    KEY idx_un_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户站内通知';
