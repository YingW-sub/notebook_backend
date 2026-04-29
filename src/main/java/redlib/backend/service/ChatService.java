package redlib.backend.service;

import redlib.backend.model.ChatMessage;

import java.util.List;

/**
 * AI 聊天对话
 * 
 * @date 2026-03-20
 */
public interface ChatService {

    /**
     * 发送消息并获取AI回复
     *
     * @param sessionId 会话ID
     * @param message  用户消息
     * @return AI的回复文本
     */
    String sendMessage(String sessionId, String message);

    /**
     * 发送消息并获取AI回复（指定模型）
     *
     * @param sessionId 会话ID
     * @param message  用户消息
     * @param model    模型标识（qwen / deepseek / kimi），为 null 时使用默认模型
     * @return AI的回复文本
     */
    String sendMessage(String sessionId, String message, String model);

    /**
     * 获取会话的历史消息列表
     *
     * @param sessionId 会话ID
     * @return 历史消息列表
     */
    List<ChatMessage> getChatHistory(String sessionId);
}
