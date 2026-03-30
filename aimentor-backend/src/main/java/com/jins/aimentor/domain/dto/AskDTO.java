package com.jins.aimentor.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AskDTO {

    /**
     * 问题内容（必填，最大 500 字）
     */
    @NotBlank(message = "问题不能为空")
    @Size(max = 500, message = "问题长度不能超过500字")
    private String question;

    /**
     * 会话 ID（可选，用于多轮对话上下文）
     * 同一 sessionId 的对话会被 AI 记住，实现多轮问答。
     * 若不传，后端自动使用 "default"。
     */
    private String sessionId;
}