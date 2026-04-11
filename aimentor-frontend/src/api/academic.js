/**
 * 学情分析模块 API
 *
 * 接口一览（所有接口均使用当前登录用户路径，前端无需传 userId）：
 *
 * 【成绩记录】
 *   GET  /api/academic/scores           -> 获取当前学生所有成绩记录
 *   POST /api/academic/score            -> 添加/更新成绩记录（id存在则更新，否则添加）
 *   DELETE /api/academic/score/{id}     -> 删除指定成绩记录
 *
 * 【知识点掌握度】
 *   GET  /api/academic/masteries        -> 获取当前学生所有知识点掌握度
 *   POST /api/academic/mastery          -> 添加/更新知识点掌握度（id存在则更新，否则添加）
 *   DELETE /api/academic/mastery/{id}   -> 删除指定知识点掌握度记录
 *
 * 【Dashboard 数据】
 *   GET  /api/academic/trend             -> 获取当前学生成绩趋势（折线图用）
 *   GET  /api/academic/weakpoints        -> 获取当前学生薄弱知识点（< 60%）
 */

import request from '@/utils/request'

// ==================== 成绩记录接口 ====================

/**
 * 获取当前登录学生的所有成绩记录
 * 用于 RecordManage 页面成绩表格展示
 *
 * @returns {Promise} 成绩记录列表，按考试日期降序
 */
export const getScoreRecords = () => {
  return request({
    url: '/academic/scores',
    method: 'get'
  })
}

/**
 * 添加或更新成绩记录
 * 判断逻辑：data.id != null 时调用 PUT 更新，否则调用 POST 新增。
 * 前端统一调此方法，无需区分添加/编辑。
 *
 * @param {Object} data
 * @param {number} [data.id]              记录ID（编辑时传入，添加时可不传）
 * @param {string} data.examDate           考试日期，格式 YYYY-MM-DD
 * @param {string} data.subject            科目名称
 * @param {number} data.score              得分
 * @param {number} data.fullScore          满分
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
 * 删除指定的成绩记录
 *
 * @param {number} id 成绩记录ID
 * @returns {Promise}
 */
export const deleteScoreRecord = (id) => {
  return request({
    url: `/academic/score/${id}`,
    method: 'delete'
  })
}

// ==================== 知识点掌握度接口 ====================

/**
 * 获取当前学生的所有知识点掌握度记录
 * 用于 RecordManage 页面知识点表格展示
 *
 * @returns {Promise} 知识点掌握度列表，按掌握度升序（薄弱在前）
 */
export const getKnowledgeMasteries = () => {
  return request({
    url: '/academic/masteries',
    method: 'get'
  })
}

/**
 * 添加或更新知识点掌握度记录
 * 判断逻辑：data.id != null 时更新，否则新增。
 * 前端统一调此方法，点击编辑时传入 id，点击添加时不传。
 *
 * @param {Object} data
 * @param {number} [data.id]       记录ID（编辑时传入）
 * @param {string} data.knowledge  知识点名称
 * @param {number} data.mastery     掌握度（0-100）
 * @param {string} [data.subject]   所属科目（可选，建议填写）
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
 * 删除指定的知识点掌握度记录
 *
 * @param {number} id 知识点记录ID
 * @returns {Promise}
 */
export const deleteKnowledgeMastery = (id) => {
  return request({
    url: `/academic/mastery/${id}`,
    method: 'delete'
  })
}

// ==================== Dashboard 数据接口 ====================

/**
 * 获取当前学生的成绩趋势（折线图用）
 * 后端按月聚合平均分，返回 [{dateMonth: "2026-01", avgScore: 85.5}, ...]
 *
 * @returns {Promise} 成绩趋势列表
 */
export const getScoreTrend = () => {
  return request({
    url: '/academic/trend',
    method: 'get'
  })
}

/**
 * 获取当前学生的薄弱知识点（掌握度 < 60%）
 * 用于 Dashboard 薄弱知识点列表展示
 *
 * @returns {Promise} 薄弱知识点列表
 */
export const getWeakPoints = () => {
  return request({
    url: '/academic/weakpoints',
    method: 'get'
  })
}

// ==================== 教师查看指定学生学情 ====================

export const getScoreTrendForStudent = (studentId) => {
  return request({
    url: `/academic/trend/${studentId}`,
    method: 'get'
  })
}

export const getWeakPointsForStudent = (studentId) => {
  return request({
    url: `/academic/weakpoints/${studentId}`,
    method: 'get'
  })
}

export const getScoreRecordsForStudent = (studentId) => {
  return request({
    url: `/academic/scores/${studentId}`,
    method: 'get'
  })
}

export const getKnowledgeMasteriesForStudent = (studentId) => {
  return request({
    url: `/academic/masteries/${studentId}`,
    method: 'get'
  })
}
