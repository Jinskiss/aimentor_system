/**
 * 学情分析模块 API
 * 包含成绩趋势、薄弱知识点等接口
 */

import request from '@/utils/request'

/**
 * 获取成绩趋势
 * @param {number} studentId 学生ID
 * @returns {Promise} 成绩趋势数据
 */
export const getScoreTrend = (studentId) => {
  return request({
    url: `/academic/trend/${studentId}`,
    method: 'get'
  })
}

/**
 * 获取薄弱知识点
 * @param {number} studentId 学生ID
 * @returns {Promise} 薄弱知识点列表
 */
export const getWeakPoints = (studentId) => {
  return request({
    url: `/academic/weakpoints/${studentId}`,
    method: 'get'
  })
}