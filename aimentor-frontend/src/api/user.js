import request from '@/utils/request'

/**
 * 用户登录
 * @param {Object} data {username, password}
 */
export const login = (data) => {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

/**
 * 用户注册
 * @param {Object} data {username, password, name, email, role}
 */
export const register = (data) => {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

/**
 * 获取用户信息
 */
export const getUserInfo = () => {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

/**
 * 发送验证码
 * @param {string} phone 手机号
 */
export const sendCode = (phone) => {
  return request({
    url: '/user/sendCode',
    method: 'post',
    data: { phone }
  })
}

/**
 * 更新个人资料（姓名/邮箱/手机/性别/年级/简介）
 * @param {Object} data { name, email, phone, gender, grade, bio }
 */
export const updateUserInfo = (data) => {
  return request({
    url: '/user/update',
    method: 'put',
    data
  })
}