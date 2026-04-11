/**
 * Axios 请求封装
 * 统一处理请求拦截、响应拦截、错误处理
 */

import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

/**
 * 从异常响应体提取可读错误信息（项目统一 Result 与 Spring Boot 默认错误 JSON 均可）
 */
function resolveResponseErrorMessage(data) {
  if (data == null) return null
  if (typeof data === 'string') return data
  if (typeof data !== 'object') return null

  const fromMsg = data.msg ?? data.message
  if (fromMsg != null && String(fromMsg).trim() !== '') {
    return String(fromMsg)
  }

  if (Array.isArray(data.errors) && data.errors.length) {
    const parts = data.errors
      .map((e) => (e && (e.defaultMessage || e.message)) || '')
      .filter(Boolean)
    if (parts.length) return parts.join('；')
  }

  if (typeof data.error === 'string' && data.error.trim() !== '') {
    if (data.error !== 'Internal Server Error') return data.error
  }

  // 兜底：过滤掉常见技术错误关键词，不展示给用户
  const raw = data.msg ?? data.message ?? ''
  const skipPatterns = [
    'SQLException', 'SQLState', 'Connection', 'DataSource',
    '郑', 'table', 'database', 'Column', 'Row',
    'NullPointer', 'ClassCast', 'JSON', 'parse', 'deserialize',
    'timeout', 'refused', 'unavailable'
  ]
  if (skipPatterns.some(p => raw.includes(p))) {
    return null  // 让外层走通用提示
  }

  return null
}

// 创建axios实例
const request = axios.create({
  baseURL: '/api',  // 后端API基础地址
  timeout: 10000,  // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从localStorage获取token（避免在拦截器中引入store导致循环依赖）
    const token = localStorage.getItem('token')
    
    // 如果有token，添加到请求头
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 如果响应是blob类型（下载文件），直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    const res = response.data
    
    // ===== 关键修复：判断响应是否存在 =====
    if (!res) {
      ElMessage.error('响应数据为空')
      return Promise.reject(new Error('响应数据为空'))
    }
    
    // ===== 统一 code 为数字，方便业务代码直接用 === 200 =====
    res.code = Number(res.code)

    // 如果code不是200，说明有错误
    if (res.code !== 200) {
      const msg = res.msg || res.message || '请求失败'
      ElMessage.error(msg)
      
      // 401未授权，清除token并跳转到登录页
      if (res.code === 401) {
        localStorage.removeItem('token')
        window.location.href = '/login'
      }
      
      // 返回reject，让调用方catch
      return Promise.reject(new Error(msg))
    }
    
    // ===== 关键修复：返回完整响应对象 =====
    // 让调用方可以获取 res.code, res.msg, res.data
    return res
  },
  (error) => {
    console.error('响应错误:', error)
    
    // 处理网络错误
    let msg = '网络错误，请稍后重试'
    
    if (error.response) {
      // 服务器返回了错误状态码
      const status = error.response.status
      const data = error.response.data
      
      if (status === 401) {
        msg = '登录已过期，请重新登录'
        localStorage.removeItem('token')
        window.location.href = '/login'
      } else if (status === 403) {
        msg = '没有权限访问'
      } else if (status === 404) {
        msg = '请求的资源不存在'
      } else if (status === 500) {
        msg = resolveResponseErrorMessage(data) || '服务器内部错误'
      } else {
        msg = resolveResponseErrorMessage(data) || `请求失败(${status})`
      }
    } else if (error.request) {
      // 请求发出但没有收到响应
      msg = '服务器无响应，请检查网络'
    } else {
      // 请求配置出错
      msg = error.message
    }
    
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request