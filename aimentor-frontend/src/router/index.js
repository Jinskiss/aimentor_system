/**
 * Vue Router 配置
 */

import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '学情分析' }
      },
      {
        path: 'record-manage',
        name: 'RecordManage',
        component: () => import('@/views/RecordManage.vue'),
        meta: { title: '记录管理' }
      },
      {
        path: 'plan',
        name: 'Plan',
        component: () => import('@/views/Plan.vue'),
        meta: { title: '学习计划' }
      },
      {
        path: 'qa',
        name: 'Qa',
        component: () => import('@/views/Qa.vue'),
        meta: { title: 'AI问答' }
      },
      {
        path: 'resource',
        name: 'Resource',
        component: () => import('@/views/Resource.vue'),
        meta: { title: '资源推荐' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  console.log('路由守卫:', to.path, 'from:', from.path)
  
  const token = localStorage.getItem('token')
  const isLoggedIn = !!token
  
  console.log('是否已登录:', isLoggedIn)
  
  // 访问公开页面（登录/注册）
  if (to.meta.public) {
    // 已登录用户访问登录页，跳转到首页
    if (isLoggedIn && to.path === '/login') {
      console.log('已登录，跳转到/dashboard')
      next('/dashboard')
      return
    }
    next()
    return
  }
  
  // 访问需要登录的页面
  if (!isLoggedIn) {
    console.log('未登录，跳转到/login')
    next('/login')
    return
  }
  
  next()
})

export default router