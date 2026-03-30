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

/**
 * 添加成绩记录
 * @param {Object} data {examDate, subject, score, fullScore}
 * @returns {Promise}
 */
export const addScoreRecord = (data) => {
  return request({
    url: '/academic/score',
    method: 'post',
    data
  })
}

/**
 * 添加知识点掌握度
 * @param {Object} data {knowledge, mastery, subject?}
 * @returns {Promise}
 */
export const addKnowledgeMastery = (data) => {
  return request({
    url: '/academic/mastery',
    method: 'post',
    data
  })
}

/**
 * 获取当前学生的所有成绩记录
 * @returns {Promise}
 */
export const getScoreRecords = () => {
  return request({
    url: '/academic/scores',
    method: 'get'
  })
}

/**
 * 获取当前学生的所有知识点掌握度记录
 * @returns {Promise}
 */
export const getKnowledgeMasteries = () => {
  return request({
    url: '/academic/masteries',
    method: 'get'
  })
}