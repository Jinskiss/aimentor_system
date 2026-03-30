/**
 * AI问答模块 API
 */

import request from '@/utils/request'

/**
 * AI提问
 * @param {Object} data {question}
 * @returns {Promise} 回答内容
 */
export const askQuestion = (data) => {
  return request({
    url: '/qa/ask',
    method: 'post',
    data
  })
}