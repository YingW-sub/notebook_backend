package redlib.backend.dto;

import lombok.Data;

import java.util.List;

/**
 * AI 多轮对话请求（与前端 JSON 字段一致，避免 Map 反序列化不稳定）
 */
@Data
public class AiChatRequestDTO {
    /**
     * 模型名，如 qwen-plus / deepseek-chat / moonshot-v1-8k
     */
    private String model;
    private List<AiChatMessageDTO> messages;
}
