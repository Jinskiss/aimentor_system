package com.jins.aimentor.service;

/**
 * QaService — AI 问答服务接口
 *
 * 该服务不直接对接大模型，而是作为桥梁：
 * 后端 Spring Boot 通过 HTTP 调用 Python 微服务（aimentor-ai/app.py），
 * 由 Python 服务实际调用 Ollama（本地）或 GroqCloud（云端）生成回答。
 *
 * 这样设计的好处：
 *   1. Java 专注于业务逻辑，AI 模型调用交给 Python 处理
 *   2. Python AI 生态丰富，切换模型（如 llama3 / qwen / deepseek）只需改配置
 *   3. 前端无需感知 AI 实现细节，统一走 /api/qa/* 接口
 */
public interface QaService {

    /**
     * 发送问题给 AI，返回回答文本
     *
     * @param message 用户发送的消息
     * @param sessionId 会话 ID（用于保持对话上下文）
     * @return AI 回答文本（Markdown 格式）
     */
    String ask(String message, String sessionId);

    /**
     * 清空指定会话的对话历史
     *
     * @param sessionId 会话 ID
     */
    void resetSession(String sessionId);
}
