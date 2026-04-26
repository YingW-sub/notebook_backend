package redlib.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import redlib.backend.annotation.Privilege;
import redlib.backend.model.ChatMessage;
import redlib.backend.service.ChatService;

import java.util.List;
import java.util.Map;

/**
 * AI聊天对话
 * @date 2026-03-20
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 发送消息
     *
     * @return AI的回复
     */
    @PostMapping("/send")
    @Privilege
    public Map<String, String> send(@RequestBody Map<String, String> request) {
        String sessionId = request.get("sessionId");
        String message = request.get("message");
        String model = request.get("model");
        String reply = chatService.sendMessage(sessionId, message, model);
        return Map.of("reply", reply);
    }

    /**
     * 获取会话历史
     *
     * @param sessionId 会话ID
     * @return 历史消息列表
     */
    @GetMapping("/history")
    @Privilege
    public List<ChatMessage> history(@RequestParam("sessionId") String sessionId) {
        return chatService.getChatHistory(sessionId);
    }
}
