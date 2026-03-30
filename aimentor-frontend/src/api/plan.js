/**
 * 学习计划模块 API
 */

import request from '@/utils/request'

/**
 * 生成学习计划
 * @param {Object} data {subject, days}
 * @returns {Promise}
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
    url: '/api/plan/my',
    method: 'get'
  })
}

/**
 * 更新计划状态
 * @param {number} id 计划ID
 * @param {Object} data {status}
 * @returns {Promise}
 */
export const updatePlanStatus = (id, data) => {
  return request({
    url: `/api/plan/${id}/status`,
    method: 'put',
    data
  })
}