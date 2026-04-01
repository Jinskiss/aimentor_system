/**
 * 学习计划模块 API
 *
 * 接口说明：
 *   POST /plan/generate  — 生成学习计划 { subject, days }
 *   GET  /plan/my       — 获取当前用户的全部学习计划
 *   GET  /plan/{id}    — 获取指定计划的详情
 *   PUT  /plan/{id}/status — 更新计划状态（已完成/未完成）
 *   PUT  /plan/{id}/complete — 标记计划为已完成
 *   DELETE /plan/{id}  — 删除指定计划
 */

import request from '@/utils/request'

/**
 * 生成学习计划
 * @param {Object} data { subject: string, days: number }
 * @returns {Promise} 生成的学习计划
 */
export const generatePlan = (data) => {
  return request({
    url: '/plan/generate',
    method: 'post',
    data
  })
}

/**
 * 获取我的学习计划
 * @returns {Promise} 计划列表
 */
export const getMyPlans = () => {
  return request({
    url: '/plan/my',
    method: 'get'
  })
}

/**
 * 获取学习计划详情
 * @param {number} id 计划ID
 * @returns {Promise} 计划详情
 */
export const getPlanById = (id) => {
  return request({
    url: `/plan/${id}`,
    method: 'get'
  })
}

/**
 * 更新计划状态
 * @param {number} id 计划ID
 * @param {string} status 新状态（已完成/未完成）
 * @returns {Promise}
 */
export const updatePlanStatus = (id, status) => {
  return request({
    url: `/plan/${id}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 标记计划为已完成
 * @param {number} id 计划ID
 * @returns {Promise}
 */
export const completePlan = (id) => {
  return request({
    url: `/plan/${id}/complete`,
    method: 'put'
  })
}

/**
 * 删除学习计划
 * @param {number} id 计划ID
 * @returns {Promise}
 */
export const deletePlan = (id) => {
  return request({
    url: `/plan/${id}`,
    method: 'delete'
  })
}
