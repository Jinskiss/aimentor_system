# -*- coding: utf-8 -*-
"""
AI 问答服务 - config.py
支持三种模式：ollama（本地）/ groq（云端免费）/ ollama_cloud（第三方托管）
"""

import os
from dotenv import load_dotenv

load_dotenv()

# ---- 基础配置 ----
HOST = os.getenv("HOST", "127.0.0.1")
PORT = int(os.getenv("PORT", "8000"))
LOG_LEVEL = os.getenv("LOG_LEVEL", "INFO")

# ---- 模型配置 ----
# ollama / groq / ollama_cloud
PROVIDER = os.getenv("PROVIDER", "ollama")

# Ollama 本地（免费）
OLLAMA_BASE_URL = os.getenv("OLLAMA_BASE_URL", "http://localhost:11434")
OLLAMA_MODEL    = os.getenv("OLLAMA_MODEL", "llama3.2")
OLLAMA_TIMEOUT = int(os.getenv("OLLAMA_TIMEOUT", "120"))

# GroqCloud（免费，无需本地部署）
GROQ_API_KEY = os.getenv("GROQ_API_KEY", "")
GROQ_MODEL   = os.getenv("GROQ_MODEL", "deepseek-r1-distill-qwen-7b")

# Ollama Cloud 第三方托管
OLLAMA_CLOUD_BASE_URL = os.getenv("OLLAMA_CLOUD_BASE_URL", "")
OLLAMA_CLOUD_MODEL    = os.getenv("OLLAMA_CLOUD_MODEL", "deepseek-r1")

# ---- 对话历史配置 ----
MAX_HISTORY = int(os.getenv("MAX_HISTORY", "20"))

# ---- 系统提示词 ----
SYSTEM_PROMPT = os.getenv(
    "SYSTEM_PROMPT",
    "你是一位耐心、专业的 AI 学习助手。你擅长用通俗易懂的语言解释概念，"
    "善于举例子和类比，帮助学生理解知识点。请用中文回答，保持回答清晰有条理，"
    "如果涉及到代码，请用 Markdown 代码块包裹。"
)

def print_config():
    """打印当前配置（启动时展示，便于排查问题）"""
    print("=" * 60)
    print("AI 问答服务配置")
    print("=" * 60)
    print(f"  Provider   : {PROVIDER}")
    if PROVIDER == "ollama":
        print(f"  Base URL   : {OLLAMA_BASE_URL}")
        print(f"  Model      : {OLLAMA_MODEL}")
        print(f"  Timeout    : {OLLAMA_TIMEOUT}s")
    elif PROVIDER == "groq":
        key = GROQ_API_KEY
        print(f"  API Key    : {key[:6] + '***' + key[-4:] if len(key) > 10 else '***'}")
        print(f"  Model      : {GROQ_MODEL}")
    elif PROVIDER == "ollama_cloud":
        print(f"  Base URL   : {OLLAMA_CLOUD_BASE_URL}")
        print(f"  Model      : {OLLAMA_CLOUD_MODEL}")
    print(f"  Max History: {MAX_HISTORY}")
    print("=" * 60)
