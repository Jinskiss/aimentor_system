import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器（保持不变）
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器：统一转换 code 为数字
request.interceptors.response.use(
  response => {
    const res = response.data
    // 如果存在 code 字段，将其转换为数字
    if (res.code !== undefined) {
      res.code = Number(res.code)
    }
    // 现在可以安全地使用 !== 进行严格比较
    if (res.code !== 200) {
      return Promise.reject(new Error(res.msg || 'Error'))
    }
    return res
  },
  error => Promise.reject(error)
)

export default request