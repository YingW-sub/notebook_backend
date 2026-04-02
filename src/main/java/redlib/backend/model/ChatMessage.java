package redlib.backend.model;

import java.util.Date;
import lombok.Data;

/**
 * 描述:chat_message表的实体类（聊天消息）
 * @version
 * @author:  18622
 * @创建时间: 2026-03-17
 */
@Data
public class ChatMessage {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID（数据隔离）
     */
    private Long userId;

    /**
     * 会话ID（用于连接上下文）
     */
    private String sessionId;

    /**
     * 角色（user-用户，assistant-AI）
     */
    private String role;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 消息内容
     */
    private String messageContent;
}
