package redlib.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import redlib.backend.dao.ChatMessageMapper;
import redlib.backend.model.ChatMessage;
import redlib.backend.service.AiService;
import redlib.backend.service.ChatService;
import redlib.backend.utils.ThreadContextHolder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 聊天服务实现类
 *
 * @author 18622
 * @date 2026-03-20
 */
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private AiService aiService;

    private static final int MAX_HISTORY_MESSAGES = 20;

    @Override
    public String sendMessage(String sessionId, String message) {
        return sendMessage(sessionId, message, null);
    }

    @Override
    public String sendMessage(String sessionId, String message, String model) {
        Assert.hasText(sessionId, "会话ID不能为空");
        Assert.hasText(message, "消息内容不能为空");

        Long userId = ThreadContextHolder.getToken().getUserId().longValue();

        ChatMessage userMsg = new ChatMessage();
        userMsg.setUserId(userId);
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setMessageContent(message);
        userMsg.setCreateTime(new Date());
        chatMessageMapper.insertSelective(userMsg);

        List<ChatMessage> history = chatMessageMapper.selectBySessionIdOrderByTime(userId, sessionId, MAX_HISTORY_MESSAGES);
        Collections.reverse(history);

        List<Map<String, String>> aiMessages = history.stream()
                .map(msg -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("role", msg.getRole());
                    m.put("content", msg.getMessageContent());
                    return m;
                })
                .collect(Collectors.toList());

        String reply = aiService.chat(aiMessages, model);

        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setUserId(userId);
        assistantMsg.setSessionId(sessionId);
        assistantMsg.setRole("assistant");
        assistantMsg.setMessageContent(reply);
        assistantMsg.setCreateTime(new Date());
        chatMessageMapper.insertSelective(assistantMsg);

        return reply;
    }

    @Override
    public List<ChatMessage> getChatHistory(String sessionId) {
        Assert.hasText(sessionId, "会话ID不能为空");

        Long userId = ThreadContextHolder.getToken().getUserId().longValue();
        return chatMessageMapper.selectAllBySessionId(userId, sessionId);
    }
}
