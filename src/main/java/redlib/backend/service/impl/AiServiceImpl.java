package redlib.backend.service.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redlib.backend.service.AiService;

import java.util.*;

/**
 * AI大模型服务实现类
 * <p>
 * 使用 Spring AI 框架，统一调用通义千问、DeepSeek、Kimi 三个模型。
 * 支持按模型标识前缀动态路由（qwen / deepseek / moonshot or kimi），
 * 并通过请求级参数覆盖在同一 ChatModel 上动态切换具体模型版本（如 qwen-max、deepseek-coder）。
 * </p>
 *
 * @author wyyy
 * @date 2026-03-18
 */
@Service
public class AiServiceImpl implements AiService {

    /**
     * 通义千问 ChatModel（配置 baseUrl + apiKey，支持 qwen-plus / qwen-max / qwen-turbo 等）
     */
    @Autowired
    @Qualifier("qwenChatModel")
    private OpenAiChatModel qwenChatModel;

    /**
     * DeepSeek ChatModel（配置 baseUrl + apiKey，支持 deepseek-chat / deepseek-coder 等）
     */
    @Autowired
    @Qualifier("deepseekChatModel")
    private OpenAiChatModel deepseekChatModel;

    /**
     * Kimi（Moonshot）ChatModel（配置 baseUrl + apiKey，支持 moonshot-v1-8k / moonshot-v1-32k 等）
     */
    @Autowired
    @Qualifier("kimiChatModel")
    private OpenAiChatModel kimiChatModel;

    /**
     * 默认模型名称，如 qwen-plus，用于填充空 model 参数时的解析来源
     */
    @Value("${spring.ai.model.qwen.model:qwen-plus}")
    private String defaultModelName;

    /**
     * 默认摘要最大 token 数
     */
    @Value("${spring.ai.openai.chat.options.max-tokens:500}")
    private int maxTokens;

    /**
     * 默认 temperature
     */
    @Value("${spring.ai.openai.chat.options.temperature:0.7}")
    private double temperature;

    /**
     * 摘要专用 prompt
     */
    @Value("${ai.summary.prompt:请简要概括以下内容的要点，最多50字：}")
    private String summaryPrompt;

    /**
     * 通用文本处理时的系统提示词
     */
    private static final String SYSTEM_PROMPT = "你是一个专业的文本处理助手。";

    private static final int MAX_CONTENT_LENGTH = 4000;

    // ==================== 公开接口 ====================

    @Override
    public String generateSummary(String content) {
        return generateSummary(content, null);
    }

    @Override
    public String generateSummary(String content, String model) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String truncated = truncateContent(content);
        return processText(summaryPrompt, truncated, model);
    }

    @Override
    public String processText(String prompt, String content) {
        return processText(prompt, content, null);
    }

    @Override
    public String processText(String prompt, String content, String model) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String resolvedModel = resolveTargetModel(model);
        OpenAiChatModel chatModel = resolveChatModelByModelName(resolvedModel);
        List<Message> messages = List.of(
                new SystemMessage(SYSTEM_PROMPT),
                new UserMessage(prompt + "\n\n" + content)
        );
        return doCall(chatModel, messages, resolvedModel);
    }

    @Override
    public String chat(List<Map<String, String>> messages) {
        return chat(messages, null);
    }

    @Override
    public String chat(List<Map<String, String>> messages, String model) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }
        String resolvedModel = resolveTargetModel(model);
        OpenAiChatModel chatModel = resolveChatModelByModelName(resolvedModel);
        List<Message> aiMessages = convertToSpringAiMessages(messages);
        return doCall(chatModel, aiMessages, resolvedModel);
    }

    @Override
    public String getDefaultModel() {
        return defaultModelName;
    }

    // ==================== 内部方法 ====================

    /**
     * 将外部传入的 model 参数归一化为目标模型名称。
     * <p>
     * 若为 null 或空字符串，以 {@link #defaultModelName} 兜底；
     * 否则直接返回传入值（保留原始大小写，供 {@link #resolveChatModelByModelName(String)} 做前缀匹配）。
     *
     * @param model 外部传入的模型标识或版本名
     * @return 确定要调用的目标模型名称
     */
    private String resolveTargetModel(String model) {
        if (model == null || model.isBlank()) {
            return defaultModelName;
        }
        return model.trim();
    }

    /**
     * 根据目标模型名称前缀路由到对应的 ChatModel Bean。
     * <p>
     * 匹配规则（按优先级顺序）：
     * <ul>
     *   <li>包含 "qwen"（不区分大小写）&rarr; {@link #qwenChatModel}</li>
     *   <li>包含 "deepseek" &rarr; {@link #deepseekChatModel}</li>
     *   <li>包含 "moonshot" 或 "kimi" &rarr; {@link #kimiChatModel}</li>
     * </ul>
     *
     * @param modelName 目标模型名称（由 {@link #resolveTargetModel(String)} 产生）
     * @return 对应的 OpenAiChatModel 实例
     */
    private OpenAiChatModel resolveChatModelByModelName(String modelName) {
        String lower = modelName.toLowerCase();
        if (lower.contains("qwen")) {
            return qwenChatModel;
        }
        if (lower.contains("deepseek")) {
            return deepseekChatModel;
        }
        if (lower.contains("moonshot") || lower.contains("kimi")) {
            return kimiChatModel;
        }
        throw new IllegalArgumentException(
                "无法从模型名称 \"" + modelName + "\" 识别出对应的服务商，"
                        + "请确保名称中包含 qwen / deepseek / moonshot / kimi 之一");
    }

    /**
     * 调用 ChatModel 执行对话。
     * <p>
     * 通过 {@link OpenAiChatOptions#model(String)} 在请求级别覆盖 ChatModel 出 bean 时配置的默认模型，
     * 从而实现同一 ChatModel Bean 调用不同模型版本。
     *
     * @param chatModel   被路由到的 ChatModel（来自 {@link #resolveChatModelByModelName(String)}）
     * @param messages    待发送的消息列表
     * @param targetModel 实际要调用的模型名称（会被动态写入 options）
     * @return AI 返回的文本内容
     */
    private String doCall(OpenAiChatModel chatModel, List<Message> messages, String targetModel) {
        try {
            ChatClient chatClient = ChatClient.create(chatModel);
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .model(targetModel)
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .build();
            String response = chatClient.prompt()
                    .messages(messages)
                    .options(options)
                    .call()
                    .content();
            return response != null ? response : "";
        } catch (Exception e) {
            throw new RuntimeException("调用AI服务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将外部消息格式 List&lt;Map&lt;String, String&gt;&gt; 转换为 Spring AI 的 Message 列表
     */
    private List<Message> convertToSpringAiMessages(List<Map<String, String>> messages) {
        List<Message> result = new ArrayList<>();
        for (Map<String, String> msg : messages) {
            String role = msg.get("role");
            String content = msg.get("content");
            if (role == null || content == null) {
                continue;
            }
            switch (role.toLowerCase()) {
                case "system" -> result.add(new SystemMessage(content));
                case "user" -> result.add(new UserMessage(content));
                case "assistant" -> result.add(new AssistantMessage(content));
                default -> result.add(new UserMessage(content));
            }
        }
        return result;
    }

    private String truncateContent(String content) {
        if (content.length() <= MAX_CONTENT_LENGTH) {
            return content;
        }
        return content.substring(0, MAX_CONTENT_LENGTH) + "...";
    }
}
