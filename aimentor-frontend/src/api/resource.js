/**
 * 资源推荐模块 API
 */

import request from '@/utils/request'

/**
 * 获取推荐资源列表
 */
export const getRecommendResources = () => {
  return request({
    url: '/resource/recommend',
    method: 'get'
  })
}

/**
 * 按科目筛选资源
 */
export const getResourcesBySubject = (subject) => {
  return request({
    url: `/resource/subject/${subject}`,
    method: 'get'
  })
}

/**
 * 按类型筛选资源
 */
export const getResourcesByType = (type) => {
  return request({
    url: `/resource/type/${type}`,
    method: 'get'
  })
}

/**
 * 按 ID 获取资源详情（站内详情页）
 */
export const getResourceById = (id) => {
  return request({
    url: `/resource/detail/${id}`,
    method: 'get'
  })
}

/**
 * 新增资源（教师端）
 */
export const createResource = (data) => {
  return request({
    url: '/resource/create',
    method: 'post',
    data
  })
}

/**
 * 更新资源（教师端）
 */
export const updateResource = (id, data) => {
  return request({
    url: `/resource/update/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除资源（教师端）
 */
export const deleteResource = (id) => {
  return request({
    url: `/resource/delete/${id}`,
    method: 'delete'
  })
}
