import request from '@/utils/request'

// 获取推荐资源
export function getRecommendResources() {
  return request({
    url: '/resource/recommend',
    method: 'get'
  })
}