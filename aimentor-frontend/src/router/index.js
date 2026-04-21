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
    redirect: to => {
      const token = localStorage.getItem('token')
      const role = localStorage.getItem('userRole')
      // 未登录直接跳转登录页
      if (!token) return '/login'
      // 根据角色跳转到对应首页
      if (role === 'admin') return '/admin/dashboard'
      if (role === 'teacher') return '/teacher/dashboard'
      return '/dashboard'
    },
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
        meta: { title: '学习资料' }
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
        path: 'teacher/profile',
        name: 'TeacherProfile',
        component: () => import('@/views/TeacherProfile.vue'),
        meta: { title: '个人中心', roles: ['teacher'] }
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
      },
      {
        path: 'teacher/plan',
        name: 'TeacherPlan',
        component: () => import('@/views/TeacherPlan.vue'),
        meta: { title: '班级计划', roles: ['teacher'] }
      },
      {
        path: 'teacher/resource',
        name: 'TeacherResource',
        component: () => import('@/views/TeacherResource.vue'),
        meta: { title: '学习资料', roles: ['teacher'] }
      },
      {
        path: 'teacher/notes',
        name: 'TeacherNotes',
        component: () => import('@/views/TeacherNotes.vue'),
        meta: { title: '班级笔记', roles: ['teacher'] }
      },
      {
        path: 'teacher/ai',
        name: 'TeacherAI',
        component: () => import('@/views/TeacherAI.vue'),
        meta: { title: 'AI分析助手', roles: ['teacher'] }
      },
      // ===== 管理端路由 =====
      {
        path: 'admin/dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/AdminDashboard.vue'),
        meta: { title: '管理首页', roles: ['admin'] }
      },
      {
        path: 'admin/users',
        name: 'AdminUsers',
        component: () => import('@/views/AdminUsers.vue'),
        meta: { title: '用户管理', roles: ['admin'] }
      },
      {
        path: 'admin/resources',
        name: 'AdminResources',
        component: () => import('@/views/AdminResources.vue'),
        meta: { title: '资源管理', roles: ['admin'] }
      },
      {
        path: 'admin/logs',
        name: 'AdminLogs',
        component: () => import('@/views/AdminLogs.vue'),
        meta: { title: '日志管理', roles: ['admin'] }
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
  const userRole = localStorage.getItem('userRole')
  const isLoggedIn = !!token

  // 公开页面（登录/注册）直接放行
  if (to.meta.public) {
    next()
    return
  }

  // 需要登录但未登录 → 去登录页
  if (!isLoggedIn) {
    next('/login')
    return
  }

  // 已登录访问公开页面 → 重定向到对应首页
  if (to.path === '/login') {
    if (userRole === 'admin') {
      next('/admin/dashboard')
    } else if (userRole === 'teacher') {
      next('/teacher/dashboard')
    } else {
      next('/dashboard')
    }
    return
  }

  // 检查页面权限
  if (to.meta.roles && to.meta.roles.length > 0) {
    if (!to.meta.roles.includes(userRole)) {
      // 没有权限，跳转到对应角色的首页
      if (userRole === 'admin') {
        next('/admin/dashboard')
      } else if (userRole === 'teacher') {
        next('/teacher/dashboard')
      } else {
        next('/dashboard')
      }
      return
    }
  }

  next()
})

export default router
