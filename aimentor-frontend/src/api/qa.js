/**
 * AI 问答模块 API
 *
 * 接口说明：
 *   - POST /api/qa/ask    { question, sessionId }  -> 获取 AI 回答
 *   - POST /api/qa/reset  { sessionId }            -> 清空对话历史
 *
 * sessionId 用于多轮对话上下文，Qa.vue 中固定使用 'default'。
 *
 * 注意：问答链路需经过 Java -> Python -> Ollama，本地模型首次推理常超过 10 秒。
 * 全局 axios 默认 timeout 为 10s，会导致浏览器先取消请求（Network 显示 canceled），
 * 而后端日志仍可能显示 200。此处为 ask 单独延长超时，与 aimentor-ai 默认 120s 对齐。
 */

import request from '@/utils/request'

/** 问答接口专用超时（毫秒），需大于典型大模型首 token / 生成时间 */
const QA_ASK_TIMEOUT_MS = 120000

/**
 * 发送问题给 AI，返回回答
 *
 * @param {Object} data
 * @param {string} data.question  用户问题（必填，最大 500 字）
 * @param {string} [data.sessionId] 会话 ID（可选，默认 'default'）
 * @returns {Promise} { code, data: { answer }, msg }
 */
export const askQuestion = (data) => {
  return request({
    url: '/qa/ask',
    method: 'post',
    data,
    timeout: QA_ASK_TIMEOUT_MS
  })
}

/**
 * 清空指定会话的对话历史
 *
 * @param {string} sessionId 会话 ID（默认 'default'）
 * @returns {Promise}
 */
export const resetConversation = (sessionId = 'default') => {
  return request({
    url: `/qa/reset`,
    method: 'post',
    params: { sessionId }
  })
}
