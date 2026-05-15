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

/**
 * 上传头像
 * @param {File} file 图片文件
 */
export const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/upload/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 更新头像URL
 * @param {string} avatar 头像URL
 */
export const updateAvatar = (avatar) => {
  return request({
    url: '/user/avatar',
    method: 'put',
    data: { avatar }
  })
}

/**
 * 发送邮箱验证码
 * @param {string} email 邮箱地址
 */
export const sendEmailCode = (email) => {
  return request({
    url: '/user/sendEmailCode',
    method: 'post',
    data: { email }
  })
}

/**
 * 验证邮箱验证码并更新邮箱
 * @param {string} email 邮箱地址
 * @param {string} code 验证码
 */
export const verifyEmailCode = (email, code) => {
  return request({
    url: '/user/verifyEmailCode',
    method: 'post',
    data: { email, code }
  })
}

/**
 * 修改密码
 * @param {string} oldPassword 旧密码
 * @param {string} newPassword 新密码
 */
export const changePassword = (oldPassword, newPassword) => {
  return request({
    url: '/user/changePassword',
    method: 'put',
    data: { oldPassword, newPassword }
  })
}