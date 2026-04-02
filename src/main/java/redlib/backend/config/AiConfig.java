package redlib.backend.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * AI大模型多实例配置
 * <p>
 * 从 application.properties 读取三个模型的 url、key、model 配置，
 * 手动实例化并注册三个不同的 OpenAiChatModel Bean。
 * </p>
 * <p>
 * 可用模型：qwen（阿里云通义千问）、deepseek、kimi（Moonshot）。
 * </p>
 *
 * @author 18622
 * @date 2026-03-22
 */
@Configuration
public class AiConfig {

    private static final double DEFAULT_TEMPERATURE = 0.7;
    private static final int DEFAULT_MAX_TOKENS = 500;

    /**
     * 通义千问（Qwen）ChatModel 实例
     */
    @Bean("qwenChatModel")
    public OpenAiChatModel qwenChatModel(
            @Value("${spring.ai.model.qwen.base-url}") String baseUrl,
            @Value("${spring.ai.model.qwen.api-key}") String apiKey,
            @Value("${spring.ai.model.qwen.model}") String model) {
        return buildChatModel(baseUrl, apiKey, model);
    }

    /**
     * DeepSeek ChatModel 实例
     */
    @Bean("deepseekChatModel")
    public OpenAiChatModel deepseekChatModel(
            @Value("${spring.ai.model.deepseek.base-url}") String baseUrl,
            @Value("${spring.ai.model.deepseek.api-key}") String apiKey,
            @Value("${spring.ai.model.deepseek.model}") String model) {
        return buildChatModel(baseUrl, apiKey, model);
    }

    /**
     * Kimi（Moonshot）ChatModel 实例
     */
    @Bean("kimiChatModel")
    public OpenAiChatModel kimiChatModel(
            @Value("${spring.ai.model.kimi.base-url}") String baseUrl,
            @Value("${spring.ai.model.kimi.api-key}") String apiKey,
            @Value("${spring.ai.model.kimi.model}") String model) {
        return buildChatModel(baseUrl, apiKey, model);
    }

    /**
     * 默认 ChatModel（指向通义千问），用于 @Autowired ChatModel 的默认注入点。
     */
    @Bean
    @Primary
    public OpenAiChatModel defaultChatModel(
            @Value("${spring.ai.model.qwen.base-url}") String baseUrl,
            @Value("${spring.ai.model.qwen.api-key}") String apiKey,
            @Value("${spring.ai.model.qwen.model}") String model) {
        return buildChatModel(baseUrl, apiKey, model);
    }

    private OpenAiChatModel buildChatModel(String baseUrl, String apiKey, String model) {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(model)
                .temperature(DEFAULT_TEMPERATURE)
                .maxTokens(DEFAULT_MAX_TOKENS)
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }
}
