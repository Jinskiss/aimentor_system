# -*- coding: utf-8 -*-
"""
AI 问答服务主程序（FastAPI）
部署方式：pip install -r requirements.txt && uvicorn app:app --reload --port 8000

支持三种 AI 提供者（修改 .env 中的 PROVIDER 选择）：
  ollama        - 本地 Ollama（免费，需要本地安装 https://ollama.com）
  groq          - GroqCloud 免费 Tier（需要 https://console.groq.com/keys 免费注册）
  ollama_cloud  - 第三方托管 Ollama API

Spring Boot 通过 HttpClient 调用本服务，实现 AI 对接。
"""

import logging
import httpx
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import List, Optional

from config import (
    PROVIDER, HOST, PORT, LOG_LEVEL,
    OLLAMA_BASE_URL, OLLAMA_MODEL, OLLAMA_TIMEOUT,
    GROQ_API_KEY, GROQ_MODEL,
    OLLAMA_CLOUD_BASE_URL, OLLAMA_CLOUD_MODEL,
    MAX_HISTORY, SYSTEM_PROMPT, print_config
)

# ===================== 日志配置 =====================
logging.basicConfig(
    level=getattr(logging, LOG_LEVEL, logging.INFO),
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)
logger = logging.getLogger(__name__)

# ===================== FastAPI 初始化 =====================
app = FastAPI(
    title="AI 问答服务",
    description="智能学习助手 API，支持 Ollama（本地）和 GroqCloud（云端）",
    version="1.0.0"
)

# 允许前端跨域请求
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ===================== 启动时打印配置 =====================
@app.on_event("startup")
async def startup():
    print_config()
    logger.info("=" * 50)
    logger.info("AI 问答服务启动中...")
    if PROVIDER == "ollama":
        logger.info("提供者：Ollama（本地）")
        logger.info("地址：%s", OLLAMA_BASE_URL)
        logger.info("模型：%s（首次调用会自动下载）", OLLAMA_MODEL)
        logger.info("提示：如未安装 Ollama，请访问 https://ollama.com 安装")
        logger.info("安装命令：ollama pull %s", OLLAMA_MODEL)
    elif PROVIDER == "groq":
        logger.info("提供者：GroqCloud（云端）")
        logger.info("模型：%s（免费 Tier）", GROQ_MODEL)
    elif PROVIDER == "ollama_cloud":
        logger.info("提供者：第三方 Ollama API")
        logger.info("地址：%s", OLLAMA_CLOUD_BASE_URL)
    logger.info("=" * 50)

# ===================== 对话历史存储 =====================
chat_histories: dict = {"default": []}

# ===================== 请求/响应模型 =====================

class ChatRequest(BaseModel):
    """聊天请求体"""
    message: str = Field(..., min_length=1, max_length=2000, description="用户发送的消息")
    session_id: Optional[str] = Field(default="default", description="会话 ID")

class ChatResponse(BaseModel):
    """聊天响应体"""
    answer: str = Field(..., description="AI 回答内容")
    session_id: str = Field(..., description="会话 ID")

# ===================== Ollama 调用 =====================

async def chat_ollama(message: str, history: List[dict]) -> str:
    """
    调用本地 Ollama API（OpenAI 兼容格式）
    地址：http://localhost:11434/api/chat
    """
    url = f"{OLLAMA_BASE_URL}/api/chat"

    messages = [{"role": "system", "content": SYSTEM_PROMPT}]
    messages.extend(history)
    messages.append({"role": "user", "content": message})

    payload = {
        "model": OLLAMA_MODEL,
        "messages": messages,
        "stream": False,
        "options": {
            "temperature": 0.7,
            "num_predict": 1024,
        }
    }

    logger.info("[Ollama] 请求 URL: %s, 模型: %s", url, OLLAMA_MODEL)

    try:
        async with httpx.AsyncClient(timeout=OLLAMA_TIMEOUT) as client:
            resp = await client.post(url, json=payload)
            resp.raise_for_status()
            data = resp.json()

        answer = data.get("message", {}).get("content", "")
        logger.info("[Ollama] 响应长度: %d 字符", len(answer))
        return answer.strip()

    except httpx.ConnectError:
        logger.error("[Ollama] 无法连接到 Ollama 服务，请确认 Ollama 已启动（默认端口 11434）")
        raise HTTPException(
            status_code=503,
            detail="无法连接到 Ollama 服务，请确认已在本地安装并启动 Ollama（https://ollama.com）"
        )
    except httpx.TimeoutException:
        logger.error("[Ollama] 请求超时（%d秒）", OLLAMA_TIMEOUT)
        raise HTTPException(status_code=504, detail=f"Ollama 请求超时（{OLLAMA_TIMEOUT}秒），请尝试更短的问题")
    except Exception as e:
        logger.error("[Ollama] 调用失败: %s", e)
        raise HTTPException(status_code=500, detail=f"Ollama 调用失败: {str(e)}")

# ===================== GroqCloud 调用 =====================

async def chat_groq(message: str, history: List[dict]) -> str:
    """
    调用 GroqCloud API
    DeepSeek-R1-Distill-Qwen-7B 和 Llama-3.3-70B-Instruct 均在免费 Tier 范围内
    注册地址：https://console.groq.com/keys（免费，无需信用卡）
    """
    if not GROQ_API_KEY:
        logger.error("[Groq] 未配置 GROQ_API_KEY，请在 .env 文件中设置")
        raise HTTPException(
            status_code=500,
            detail="未配置 GROQ_API_KEY，请访问 https://console.groq.com/keys 注册获取免费 Key"
        )

    url = "https://api.groq.com/openai/v1/chat/completions"

    messages = [{"role": "system", "content": SYSTEM_PROMPT}]
    messages.extend(history)
    messages.append({"role": "user", "content": message})

    payload = {
        "model": GROQ_MODEL,
        "messages": messages,
        "temperature": 0.7,
        "max_tokens": 1024,
    }

    headers = {
        "Authorization": f"Bearer {GROQ_API_KEY}",
        "Content-Type": "application/json"
    }

    logger.info("[Groq] 模型: %s, 消息长度: %d", GROQ_MODEL, len(message))

    try:
        async with httpx.AsyncClient(timeout=60) as client:
            resp = await client.post(url, json=payload, headers=headers)
            resp.raise_for_status()
            data = resp.json()

        answer = data["choices"][0]["message"]["content"]
        logger.info("[Groq] 响应长度: %d 字符", len(answer))
        return answer.strip()

    except httpx.ConnectError:
        logger.error("[Groq] 无法连接到 Groq API，请检查网络")
        raise HTTPException(status_code=503, detail="无法连接到 Groq API，请检查网络连接")
    except Exception as e:
        logger.error("[Groq] 调用失败: %s", e)
        raise HTTPException(status_code=500, detail=f"Groq 调用失败: {str(e)}")

# ===================== Ollama Cloud 调用 =====================

async def chat_ollama_cloud(message: str, history: List[dict]) -> str:
    """调用第三方托管的 Ollama API"""
    if not OLLAMA_CLOUD_BASE_URL:
        raise HTTPException(status_code=500, detail="未配置 OLLAMA_CLOUD_BASE_URL")

    url = f"{OLLAMA_CLOUD_BASE_URL}/api/chat"

    messages = [{"role": "system", "content": SYSTEM_PROMPT}]
    messages.extend(history)
    messages.append({"role": "user", "content": message})

    payload = {
        "model": OLLAMA_CLOUD_MODEL,
        "messages": messages,
        "stream": False,
    }

    logger.info("[OllamaCloud] URL: %s, 模型: %s", url, OLLAMA_CLOUD_MODEL)

    try:
        async with httpx.AsyncClient(timeout=120) as client:
            resp = await client.post(url, json=payload)
            resp.raise_for_status()
            data = resp.json()

        answer = data.get("message", {}).get("content", "")
        logger.info("[OllamaCloud] 响应长度: %d 字符", len(answer))
        return answer.strip()

    except Exception as e:
        logger.error("[OllamaCloud] 调用失败: %s", e)
        raise HTTPException(status_code=500, detail=f"OllamaCloud 调用失败: {str(e)}")

# ===================== 对话路由 =====================

@app.post("/api/chat", response_model=ChatResponse)
async def chat(req: ChatRequest):
    """
    核心聊天接口
    请求示例：POST /api/chat  Body: {"message": "什么是函数？", "session_id": "default"}
    响应示例：{"answer": "函数是...", "session_id": "default"}
    """
    session_id = req.session_id or "default"
    logger.info("[Chat] session_id=%s, 消息长度=%d, message='%s'",
                session_id, len(req.message), req.message[:50])

    if session_id not in chat_histories:
        chat_histories[session_id] = []
    history = chat_histories[session_id]

    if PROVIDER == "ollama":
        answer = await chat_ollama(req.message, history)
    elif PROVIDER == "groq":
        answer = await chat_groq(req.message, history)
    elif PROVIDER == "ollama_cloud":
        answer = await chat_ollama_cloud(req.message, history)
    else:
        raise HTTPException(status_code=500, detail=f"未知 PROVIDER: {PROVIDER}")

    history.append({"role": "user", "content": req.message})
    history.append({"role": "assistant", "content": answer})

    if len(history) > MAX_HISTORY * 2:
        chat_histories[session_id] = history[-MAX_HISTORY * 2:]
        logger.info("[Chat] 历史超过上限，已裁剪至最近 %d 条", MAX_HISTORY)

    return ChatResponse(answer=answer, session_id=session_id)


@app.post("/api/chat/reset")
async def reset_chat(session_id: str = "default"):
    """清空指定会话的历史记录"""
    if session_id in chat_histories:
        chat_histories[session_id] = []
    logger.info("[Reset] 会话 %s 历史已清空", session_id)
    return {"msg": "历史已清空", "session_id": session_id}


@app.get("/api/health")
async def health():
    """健康检查接口"""
    return {
        "status": "ok",
        "provider": PROVIDER,
        "model": OLLAMA_MODEL if PROVIDER == "ollama" else GROQ_MODEL if PROVIDER == "groq" else OLLAMA_CLOUD_MODEL,
        "history_count": sum(len(v) for v in chat_histories.values()) // 2
    }


@app.get("/")
async def root():
    """服务根路径"""
    return {
        "service": "AI 问答服务",
        "version": "1.0.0",
        "provider": PROVIDER,
        "docs": "/docs",
        "endpoints": {
            "chat": "POST /api/chat",
            "reset": "POST /api/chat/reset",
            "health": "GET /api/health"
        }
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host=HOST, port=PORT, log_level=LOG_LEVEL.lower())
