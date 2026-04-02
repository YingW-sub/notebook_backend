package redlib.backend.dto;

import lombok.Data;

/**
 * AI 多轮对话单条消息
 */
@Data
public class AiChatMessageDTO {
    private String role;
    private String content;
}
