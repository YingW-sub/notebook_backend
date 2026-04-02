package redlib.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import redlib.backend.annotation.Privilege;
import redlib.backend.dto.AiChatMessageDTO;
import redlib.backend.dto.AiChatRequestDTO;
import redlib.backend.service.AiService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI功能控制器
 *
 * @author wyyy
 * @date 2026-03-20
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @Value("${ai.polish.prompt:请帮我润色以下文本，使其语言更加专业、流畅，修正语病：}")
    private String polishPrompt;

    @Value("${ai.category.prompt:请根据以下笔记内容，提取一个最适合的分类名称（不要包含多余标点，最多6个字）：}")
    private String categoryPrompt;

    /**
     * AI生成摘要
     *
     * @param request 请求体，content 为必填，model 为可选（qwen / deepseek / kimi）
     * @return AI生成的摘要
     */
    @PostMapping("/summary")
    @Privilege
    public Map<String, Object> summary(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        String model = request.get("model");
        String summary = aiService.generateSummary(content, model);
        return Map.of("result", summary, "model", aiService.getDefaultModel());
    }

    /**
     * AI润色文本
     *
     * @param request 请求体，content 为必填，model 为可选（qwen / deepseek / kimi）
     * @return 润色后的文本
     */
    @PostMapping("/polish")
    @Privilege
    public Map<String, Object> polish(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        String model = request.get("model");
        String polished = aiService.processText(polishPrompt, content, model);
        return Map.of("result", polished, "model", aiService.getDefaultModel());
    }

    /**
     * AI智能分类
     *
     * @param request 请求体，content 为必填，model 为可选（qwen / deepseek / kimi）
     * @return 分类名称
     */
    @PostMapping("/categorize")
    @Privilege
    public Map<String, Object> categorize(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        String model = request.get("model");
        String category = aiService.processText(categoryPrompt, content, model);
        return Map.of("result", category.trim(), "model", aiService.getDefaultModel());
    }

    /**
     * 获取当前默认模型名称
     *
     * @return 默认模型信息
     */
    @GetMapping("/defaultModel")
    @Privilege
    public Map<String, String> defaultModel() {
        return Map.of("defaultModel", aiService.getDefaultModel());
    }

    /**
     * 可选的大模型（与后端三套 API Key 一一对应：通义千问 / DeepSeek / Kimi）
     */
    @GetMapping("/modelOptions")
    @Privilege
    public List<Map<String, String>> modelOptions() {
        return List.of(
                Map.of("value", "qwen-turbo", "label", "通义千问 · qwen-turbo（轻量）"),
                Map.of("value", "qwen-plus", "label", "通义千问 · qwen-plus"),
                Map.of("value", "qwen-max", "label", "通义千问 · qwen-max"),
                Map.of("value", "qwen2.5-72b-instruct", "label", "通义千问 · Qwen2.5-72B-Instruct"),
                Map.of("value", "deepseek-chat", "label", "DeepSeek · Chat（对话 / 满血版）"),
                Map.of("value", "deepseek-coder", "label", "DeepSeek · Coder"),
                Map.of("value", "deepseek-reasoner", "label", "DeepSeek · R1 推理"),
                Map.of("value", "moonshot-v1-8k", "label", "Kimi · moonshot-v1-8k"),
                Map.of("value", "moonshot-v1-32k", "label", "Kimi · moonshot-v1-32k"),
                Map.of("value", "moonshot-v1-128k", "label", "Kimi · moonshot-v1-128k（长文本）")
        );
    }

    /**
     * 多轮对话；model 传 {@link #modelOptions()} 中的 value，决定走哪一路 API Key。
     */
    @PostMapping("/chat")
    @Privilege
    public Map<String, Object> chat(@RequestBody AiChatRequestDTO body) {
        if (body == null || CollectionUtils.isEmpty(body.getMessages())) {
            return Map.of("result", "", "error", "messages 不能为空");
        }
        String model = StringUtils.hasText(body.getModel()) ? body.getModel().trim() : null;
        List<Map<String, String>> messages = body.getMessages().stream()
                .map(this::toMessageMap)
                .collect(Collectors.toList());
        String reply = aiService.chat(messages, model);
        return Map.of("result", reply);
    }

    private Map<String, String> toMessageMap(AiChatMessageDTO m) {
        Map<String, String> map = new HashMap<>(2);
        map.put("role", m.getRole() != null ? m.getRole() : "user");
        map.put("content", m.getContent() != null ? m.getContent() : "");
        return map;
    }
}
