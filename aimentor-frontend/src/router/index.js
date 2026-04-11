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
    redirect: getDefaultRoute,
    children: [
      // ===== 学生路由 =====
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '学情分析', roles: ['student'] }
      },
      {
        path: 'record-manage',
        name: 'RecordManage',
        component: () => import('@/views/RecordManage.vue'),
        meta: { title: '学情记录', roles: ['student'] }
      },
      {
        path: 'plan',
        name: 'Plan',
        component: () => import('@/views/Plan.vue'),
        meta: { title: '学习计划', roles: ['student'] }
      },
      {
        path: 'qa',
        name: 'Qa',
        component: () => import('@/views/Qa.vue'),
        meta: { title: 'AI问答', roles: ['student'] }
      },
      {
        path: 'resource',
        name: 'Resource',
        component: () => import('@/views/Resource.vue'),
        meta: { title: '资源推荐' }
      },
      {
        path: 'resource/:id',
        name: 'ResourceDetail',
        component: () => import('@/views/ResourceDetail.vue'),
        meta: { title: '资源详情' }
      },
      {
        path: 'notes',
        name: 'Notes',
        component: () => import('@/views/Notes.vue'),
        meta: { title: '学习笔记', roles: ['student'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
        meta: { title: '设置' }
      },
      // ===== 教师路由 =====
      {
        path: 'teacher/dashboard',
        name: 'TeacherDashboard',
        component: () => import('@/views/TeacherDashboard.vue'),
        meta: { title: '教师首页', roles: ['teacher'] }
      },
      {
        path: 'teacher/students',
        name: 'TeacherStudents',
        component: () => import('@/views/TeacherStudents.vue'),
        meta: { title: '学生学情', roles: ['teacher'] }
      },
      {
        path: 'teacher/student/:id',
        name: 'TeacherStudentDetail',
        component: () => import('@/views/TeacherStudentDetail.vue'),
        meta: { title: '学生学情详情', roles: ['teacher'] }
      }
    ]
  }
]

/**
 * 根据用户角色获取默认跳转路由
 */
function getDefaultRoute() {
  const role = localStorage.getItem('userRole')
  if (role === 'teacher') return '/teacher/dashboard'
  if (role === 'admin') return '/admin/dashboard'
  return '/dashboard'
}

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  console.log('路由守卫:', to.path, 'from:', from.path)

  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole')
  const isLoggedIn = !!token

  // 访问公开页面（登录/注册）
  if (to.meta.public) {
    if (isLoggedIn && to.path === '/login') {
      next('/')
      return
    }
    next()
    return
  }

  // 访问需要登录的页面
  if (!isLoggedIn) {
    next('/login')
    return
  }

  // 角色权限校验：检查 meta.roles 是否限制了访问角色
  if (to.meta.roles && to.meta.roles.length > 0) {
    if (!to.meta.roles.includes(userRole)) {
      // 角色不匹配，跳转到自己的首页
      if (userRole === 'teacher') {
        next('/teacher/dashboard')
      } else if (userRole === 'admin') {
        next('/admin/dashboard')
      } else {
        next('/dashboard')
      }
      return
    }
  }

  next()
})

export default router
