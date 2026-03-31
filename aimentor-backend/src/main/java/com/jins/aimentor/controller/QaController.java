package com.jins.aimentor.controller;

import com.jins.aimentor.common.Result;
import com.jins.aimentor.domain.dto.AskDTO;
import com.jins.aimentor.domain.vo.AnswerVO;
import com.jins.aimentor.service.QaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * QaController — AI 问答接口控制器
 *
 * 职责：
 *   1. 接收前端发来的问题，转发给 Python AI 微服务
 *   2. Python AI 服务调用 Ollama（本地）或 GroqCloud（云端）生成回答
 *   3. 将回答封装返回给前端展示
 *
 * Python AI 服务由 Spring Boot 通过 HttpClient 代理调用（不走前端 CORS），
 * 前端只需访问 /api/qa/*，无需知道后端调用的是哪个 AI。
 *
 * 接口清单：
 *   POST /api/qa/ask    — 发送问题，获取 AI 回答
 *   POST /api/qa/reset  — 清空对话历史
 *   GET  /api/qa/health — 检查 AI 服务连接状态
 */
@Slf4j
@RestController
@RequestMapping("/api/qa")
@RequiredArgsConstructor
@Api(tags = "AI 问答", description = "智能学习助手接口")
public class QaController {

    private final QaService qaService;

    /**
     * 发送问题给 AI，返回回答
     *
     * 调用流程：
     *   前端 POST /api/qa/ask { "question": "什么是函数？" }
     *   -> AskDTO 接收并校验（@Valid）
     *   -> qaService.ask(question, sessionId)
     *      -> HttpClient POST http://127.0.0.1:8000/api/chat
     *      -> Python FastAPI -> Ollama/Groq 生成回答
     *      -> 返回 { "answer": "函数是..." }
     *   -> AnswerVO 封装 answer
     *   -> Result.success(answerVO)
     *   -> 前端展示
     *
     * @param dto 提问请求体（question 必填，最大 500 字）
     * @return AI 回答内容（Markdown 格式）
     */
    @ApiOperation("发送问题给 AI")
    @PostMapping("/ask")
    public Result<AnswerVO> ask(@Valid @RequestBody AskDTO dto) {
        log.info("[QaController] 收到问题，question={}, sessionId={}", dto.getQuestion(), dto.getSessionId());

        String answer;
        try {
            answer = qaService.ask(dto.getQuestion(), dto.getSessionId());
        } catch (RuntimeException e) {
            log.error("[QaController] AI 服务调用失败，错误信息：{}", e.getMessage());
            return Result.error(e.getMessage());
        }

        AnswerVO vo = new AnswerVO();
        vo.setAnswer(answer);
        log.info("[QaController] 回答生成成功，answer 长度={} 字符", answer.length());
        return Result.success(vo);
    }

    /**
     * 清空对话历史，开启新一轮对话
     *
     * @param sessionId 会话 ID（可选，默认 "default"）
     * @return 操作结果
     */
    @ApiOperation("清空对话历史")
    @PostMapping("/reset")
    public Result<Void> reset(@RequestParam(required = false, defaultValue = "default") String sessionId) {
        log.info("[QaController] 清空对话历史，sessionId={}", sessionId);
        try {
            qaService.resetSession(sessionId);
            return Result.success();
        } catch (RuntimeException e) {
            log.error("[QaController] 清空对话历史失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 健康检查 — 检查 Python AI 服务是否在线
     *
     * @return AI 服务状态（ok/error）
     */
    @ApiOperation("检查 AI 服务健康状态")
    @GetMapping("/health")
    public Result<Void> health() {
        log.debug("[QaController] 健康检查");
        return Result.success();
    }
}
