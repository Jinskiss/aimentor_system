package com.jins.aimentor.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jins.aimentor.service.QaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * QaServiceImpl - AI 问答服务实现
 *
 * 通过 Java 11 内置的 HttpClient 调用 Python FastAPI 微服务（aimentor-ai/app.py）。
 * Python 服务再对接 Ollama（本地）或 GroqCloud（云端）生成 AI 回答。
 *
 * 调用链路：
 *   前端 POST /api/qa/ask
 *   -> QaController.ask() -> QaServiceImpl.ask()
 *   -> HttpClient POST http://127.0.0.1:8000/api/chat
 *   -> Python FastAPI -> Ollama/GroqCloud
 *   -> Python 返回 {answer: "...", session_id: "..."}
 *   -> 提取 answer 字段返回给前端
 *
 * 配置项（在 application.yml 中设置）：
 *   ai.service.base-url: http://127.0.0.1:8000
 *   ai.service.timeout-seconds: 60
 */
@Slf4j
@Service
public class QaServiceImpl implements QaService {

    @Value("${ai.service.base-url:http://127.0.0.1:8000}")
    private String aiServiceBaseUrl;

    @Value("${ai.service.timeout-seconds:60}")
    private int timeoutSeconds;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String ask(String message, String sessionId) {
        log.info("[QaService] 发送问题到 AI 服务，message={}, sessionId={}", message, sessionId);

        String requestBody;
        try {
            Map<String, String> bodyMap = Map.of(
                    "message", message,
                    "session_id", sessionId != null ? sessionId : "default"
            );
            requestBody = objectMapper.writeValueAsString(bodyMap);
        } catch (Exception e) {
            log.error("[QaService] 构造请求体失败", e);
            throw new RuntimeException("构造 AI 请求体失败", e);
        }

        String url = aiServiceBaseUrl + "/api/chat";
        log.info("[QaService] 发送请求，url={}", url);
        log.info("[QaService] 序列化后的 JSON 请求体: {}", requestBody);
        log.info("[QaService] 请求体字节数: {}", requestBody.getBytes(StandardCharsets.UTF_8).length);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            int statusCode = resp.getStatusCode().value();
            String responseBody = resp.getBody();
            log.info("[QaService] 收到响应，statusCode={}, body={}", statusCode, responseBody);

            if (statusCode != 200) {
                log.error("[QaService] AI 服务返回错误，statusCode={}, body={}", statusCode, responseBody);
                try {
                    JsonNode node = objectMapper.readTree(responseBody);
                    String detail = node.has("detail") ? node.get("detail").asText() : responseBody;
                    throw new RuntimeException("AI 服务调用失败：" + detail);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e2) {
                    throw new RuntimeException("AI 服务调用失败（HTTP " + statusCode + "）：" + responseBody);
                }
            }

            // 解析 Python 返回的 JSON：{ "answer": "...", "session_id": "..." }
            JsonNode root = objectMapper.readTree(responseBody);
            String answer = root.get("answer").asText();

            log.info("[QaService] AI 回答生成成功，answer 长度={} 字符", answer.length());
            return answer;

        } catch (org.springframework.web.client.RestClientException e) {
            log.error("[QaService] 无法连接到 Python AI 服务（http://127.0.0.1:8000），请确认 Python AI 服务已启动", e);
            throw new RuntimeException(
                    "无法连接到 AI 问答服务，请先启动 Python AI 服务（命令：cd aidescriptor-ai && pip install -r requirements.txt && uvicorn app:app --reload --port 8000）",
                    e
            );
        } catch (Exception e) {
            log.error("[QaService] 调用 AI 服务时发生异常", e);
            throw new RuntimeException("调用 AI 服务失败：" + e.getMessage(), e);
        }
    }

    @Override
    public void resetSession(String sessionId) {
        log.info("[QaService] 清空会话历史，sessionId={}", sessionId);
        String sid = sessionId != null ? sessionId : "default";
        String url = aiServiceBaseUrl + "/api/chat/reset?session_id=" + sid;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            log.info("[QaService] 清空会话响应，statusCode={}", resp.getStatusCode().value());

        } catch (Exception e) {
            log.error("[QaService] 清空会话失败", e);
            throw new RuntimeException("清空对话历史失败：" + e.getMessage(), e);
        }
    }
}
