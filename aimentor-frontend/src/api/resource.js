/**
 * 资源推荐模块 API
 */

import request from '@/utils/request'

/**
 * 获取推荐资源
 * @param {Object} params {page, size}
 * @returns {Promise} 资源列表
 */
export const getRecommendations = (params) => {
  return request({
    url: '/resource/recommend',
    method: 'get',
    params
  })
}