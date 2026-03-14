import { defineStore } from 'pinia'
import { login, register, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null
  }),
  actions: {
    async login(loginForm) {
      const res = await login(loginForm)
      if (res.code === 200) {
        this.token = res.data
        localStorage.setItem('token', res.data)
        // 获取用户信息
        await this.fetchUserInfo()
      }
      return res
    },
    async register(registerForm) {
      return await register(registerForm)
    },
    async fetchUserInfo() {
      const res = await getUserInfo()
      if (res.code === 200) {
        this.userInfo = res.data
      }
    },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
    }
  }
})