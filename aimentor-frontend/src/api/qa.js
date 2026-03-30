/**
 * AI 问答模块 API
 *
 * 接口说明：
 *   - POST /api/qa/ask    { question, sessionId }  -> 获取 AI 回答
 *   - POST /api/qa/reset  { sessionId }            -> 清空对话历史
 *
 * sessionId 用于多轮对话上下文，Qa.vue 中固定使用 'default'。
 */

import request from '@/utils/request'

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
    data
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
