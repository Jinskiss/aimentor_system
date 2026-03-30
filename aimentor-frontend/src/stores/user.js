/**
 * 用户状态管理 (Pinia)
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, getUserInfo as getUserInfoApi } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  // Getters
  const isLoggedIn = computed(() => {
    const hasToken = !!token.value
    console.log('检查登录状态:', hasToken, 'token:', token.value?.substring(0, 20) + '...')
    return hasToken
  })
  
  const username = computed(() => userInfo.value?.name || '')

  // Actions
  /**
   * 登录
   * @param {Object} loginForm {username, password}
   */
  const login = async (loginForm) => {
    try {
      console.log('调用登录API...')
      const res = await loginApi(loginForm)
      console.log('登录API响应:', res)
      
      // 获取token
      const tokenValue = res.data
      
      if (!tokenValue) {
        console.error('响应中没有token:', res)
        throw new Error('登录成功但未返回token')
      }
      
      // 保存token
      token.value = tokenValue
      localStorage.setItem('token', tokenValue)
      console.log('token已保存:', tokenValue.substring(0, 20) + '...')
      
      // 获取用户信息
      try {
        await fetchUserInfo()
      } catch (e) {
        console.warn('获取用户信息失败，但登录成功:', e)
      }
      
      return res
    } catch (error) {
      console.error('登录过程错误:', error)
      throw error
    }
  }

  /**
   * 注册
   * @param {Object} registerForm {username, password, name, email, role}
   */
  const register = async (registerForm) => {
    console.log('=== store.register 被调用，参数:', registerForm);
    
    try {
      console.log('调用注册API:', registerForm)
      const res = await registerApi(registerForm)
      console.log('注册API响应:', res)
      return res
    } catch (error) {
      console.error('注册过程错误:', error)
      throw error
    }
  }

  /**
   * 获取用户信息
   */
  const fetchUserInfo = async () => {
    try {
      const res = await getUserInfoApi()
      console.log('用户信息响应:', res)
      
      userInfo.value = res.data
      return res.data
    } catch (error) {
      console.error('获取用户信息失败:', error)
      throw error
    }
  }

  /**
   * 退出登录
   */
  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    username,
    login,
    register,  // 导出 register
    fetchUserInfo,
    logout
  }
})