import request from '@/utils/request'

// 获取成绩趋势
export function getScoreTrend(studentId) {
  return request({
    url: `/academic/trend/${studentId}`,
    method: 'get'
  })
}

// 获取薄弱知识点
export function getWeakPoints(studentId) {
  return request({
    url: `/academic/weakpoints/${studentId}`,
    method: 'get'
  })
}