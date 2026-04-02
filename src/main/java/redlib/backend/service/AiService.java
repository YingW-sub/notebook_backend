package redlib.backend.service;

import java.util.List;
import java.util.Map;

/**
 * AI大模型服务接口
 *
 * @author 18622
 * @date 2026-03-20
 */
public interface AiService {

    /**
     * 调用大模型生成摘要
     *
     * @param content 输入文本内容
     * @return AI生成的摘要
     */
    String generateSummary(String content);

    /**
     * 调用大模型生成摘要（指定模型）
     *
     * @param content 输入文本内容
     * @param model   模型标识，取值：qwen / deepseek / kimi
     * @return AI生成的摘要
     */
    String generateSummary(String content, String model);

    /**
     * 调用大模型进行通用文本处理
     *
     * @param prompt  提示词/指令
     * @param content 输入文本内容
     * @return AI处理后的结果
     */
    String processText(String prompt, String content);

    /**
     * 调用大模型进行通用文本处理（指定模型）
     *
     * @param prompt  提示词/指令
     * @param content 输入文本内容
     * @param model   模型标识，取值：qwen / deepseek / kimi
     * @return AI处理后的结果
     */
    String processText(String prompt, String content, String model);

    /**
     * 调用大模型进行多轮对话
     *
     * @param messages 消息列表，每条消息包含 role 和 content
     * @return AI的回复内容
     */
    String chat(List<Map<String, String>> messages);

    /**
     * 调用大模型进行多轮对话（指定模型）
     *
     * @param messages 消息列表，每条消息包含 role 和 content
     * @param model    模型标识，取值：qwen / deepseek / kimi
     * @return AI的回复内容
     */
    String chat(List<Map<String, String>> messages, String model);

    /**
     * 获取当前默认模型名称
     *
     * @return 默认模型名称
     */
    String getDefaultModel();
}
