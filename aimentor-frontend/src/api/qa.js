import request from '@/utils/request'

// 提问
export function askQuestion(data) {
  return request({
    url: '/qa/ask',
    method: 'post',
    data
  })
}