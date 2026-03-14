import request from '@/utils/request'

// 生成学习计划
export function generatePlan(data) {
  return request({
    url: '/plan/generate',
    method: 'post',
    data
  })
}

// 获取我的计划
export function getMyPlans() {
  return request({
    url: '/plan/my',
    method: 'get'
  })
}

// 更新计划状态（完成）
export function updatePlanStatus(planId, status) {
  return request({
    url: `/plan/${planId}/status`,
    method: 'put',
    data: { status }
  })
}