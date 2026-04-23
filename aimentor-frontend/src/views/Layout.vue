<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <!-- Logo 区域 -->
      <div class="logo" :class="{ 'logo-collapse': isCollapse }">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5"/>
            <path d="M2 12l10 5 10-5"/>
          </svg>
        </div>
        <transition name="fade">
          <h2 v-if="!isCollapse" class="logo-text">AI学业导师</h2>
        </transition>
      </div>

      <!-- 导航菜单 -->
      <el-menu
        :default-active="menuActivePath"
        router
        class="el-menu-vertical"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="#1a1a2e"
        text-color="#a0a0b0"
        active-text-color="var(--theme-color)"
      >
        <!-- ===== 学生菜单 ===== -->
        <template v-if="currentRole === 'student'">
          <el-menu-item index="/dashboard">
            <el-icon><DataLine /></el-icon>
            <template #title>学情分析</template>
          </el-menu-item>
          <el-menu-item index="/record-manage">
            <el-icon><Edit /></el-icon>
            <template #title>学情记录</template>
          </el-menu-item>
          <el-menu-item index="/plan">
            <el-icon><Calendar /></el-icon>
            <template #title>学习计划</template>
          </el-menu-item>
          <el-menu-item index="/qa">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>AI问答</template>
          </el-menu-item>
          <el-menu-item index="/resource">
            <el-icon><Collection /></el-icon>
            <template #title>资源推荐</template>
          </el-menu-item>
          <el-menu-item index="/notes">
            <el-icon><Notebook /></el-icon>
            <template #title>学习笔记</template>
          </el-menu-item>
          <el-menu-item index="/profile">
            <el-icon><User /></el-icon>
            <template #title>个人中心</template>
          </el-menu-item>
          <el-menu-item index="/settings">
            <el-icon><Setting /></el-icon>
            <template #title>设置</template>
          </el-menu-item>
        </template>

        <!-- ===== 教师菜单 ===== -->
        <template v-else-if="currentRole === 'teacher'">
          <el-menu-item index="/teacher/dashboard">
            <el-icon><DataLine /></el-icon>
            <template #title>教师首页</template>
          </el-menu-item>
          <el-menu-item index="/teacher/students">
            <el-icon><User /></el-icon>
            <template #title>学生管理</template>
          </el-menu-item>
          <el-menu-item index="/teacher/plan">
            <el-icon><Calendar /></el-icon>
            <template #title>班级计划</template>
          </el-menu-item>
          <el-menu-item index="/teacher/resource">
            <el-icon><Collection /></el-icon>
            <template #title>班级资源</template>
          </el-menu-item>
          <el-menu-item index="/teacher/notes">
            <el-icon><Notebook /></el-icon>
            <template #title>班级笔记</template>
          </el-menu-item>
          <el-menu-item index="/teacher/ai">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>AI分析助手</template>
          </el-menu-item>
          <el-menu-item index="/teacher/profile">
            <el-icon><User /></el-icon>
            <template #title>个人中心</template>
          </el-menu-item>
          <el-menu-item index="/settings">
            <el-icon><Setting /></el-icon>
            <template #title>设置</template>
          </el-menu-item>
        </template>

        <!-- ===== 管理员菜单 ===== -->
        <template v-else-if="currentRole === 'admin'">
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataLine /></el-icon>
            <template #title>管理首页</template>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/logs">
            <el-icon><Document /></el-icon>
            <template #title>日志管理</template>
          </el-menu-item>
          <el-menu-item index="/profile">
            <el-icon><UserFilled /></el-icon>
            <template #title>个人中心</template>
          </el-menu-item>
          <el-menu-item index="/settings">
            <el-icon><Setting /></el-icon>
            <template #title>设置</template>
          </el-menu-item>
        </template>
      </el-menu>

      <!-- 折叠按钮 -->
      <div class="collapse-btn" @click="isCollapse = !isCollapse">
        <el-icon v-if="isCollapse"><DArrowRight /></el-icon>
        <el-icon v-else><DArrowLeft /></el-icon>
        <span v-if="!isCollapse">收起</span>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <div class="breadcrumb">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentPageName }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
        </div>
        <div class="header-right">
          <!-- 消息通知 -->
          <el-dropdown trigger="click" placement="bottom-end">
            <div class="header-action">
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="badge-item">
                <el-icon :size="20"><Bell /></el-icon>
              </el-badge>
            </div>
            <template #dropdown>
              <el-dropdown-menu style="width: 320px; padding: 0;">
                <div class="notif-header">
                  <span class="notif-title">通知中心</span>
                  <el-button link size="small" @click.stop="markAllRead" v-if="unreadCount > 0">全部已读</el-button>
                </div>
                <div v-if="notifications.length === 0" class="notif-empty">暂无新通知</div>
                <div v-else class="notif-list">
                  <div
                    v-for="item in notifications"
                    :key="item.id"
                    :class="['notif-item', { unread: !item.read }]"
                    @click="handleNotifClick(item)"
                  >
                    <el-icon class="notif-icon">
                      <component :is="item.icon" />
                    </el-icon>
                    <div class="notif-body">
                      <p class="notif-text">{{ item.text }}</p>
                      <span class="notif-time">{{ item.time }}</span>
                    </div>
                    <div v-if="!item.read" class="notif-dot"></div>
                  </div>
                </div>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <!-- 用户信息 -->
          <div class="user-info">
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-avatar">
                <el-avatar :size="36" fit="cover" :src="userStore.userInfo?.avatar && userStore.userInfo?.avatar !== '' ? userStore.userInfo.avatar : undefined" :style="{ backgroundColor: !userStore.userInfo?.avatar || userStore.userInfo?.avatar === '' ? 'var(--theme-color)' : 'transparent' }">
                  {{ (!userStore.userInfo?.avatar || userStore.userInfo?.avatar === '') ? (userStore.username || '同学').charAt(0).toUpperCase() : '' }}
                </el-avatar>
                <div class="user-detail">
                  <span class="user-name">{{ userStore.username || '同学' }}</span>
                  <span class="user-role">{{ userRole }}</span>
                </div>
                <el-icon class="arrow-down"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="settings">
                    <el-icon><Setting /></el-icon>设置
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <!-- 内容区域 -->
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Bell,
  ArrowDown,
  User,
  UserFilled,
  Setting,
  SwitchButton,
  CircleCheck,
  ChatDotRound,
  Calendar,
  WarningFilled,
  DataLine,
  Collection,
  Notebook,
  Edit,
  DArrowLeft,
  DArrowRight,
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const isCollapse = ref(false)

const notifications = ref([
  { id: 1, type: 'success', icon: 'CircleCheck', text: '恭喜！你的学习计划「数学7天」已完成', time: '10分钟前', read: false, link: '/plan' },
  { id: 2, type: 'info',    icon: 'ChatDotRound', text: 'AI 已回答你的问题：如何提高解题速度', time: '1小时前',   read: false, link: '/qa' },
  { id: 3, type: 'info',   icon: 'Calendar',     text: '别忘了今天的学习计划，加油！',              time: '2小时前',   read: true,  link: '/plan' }
])
const unreadCount = computed(() => notifications.value.filter(n => !n.read).length)

const markAllRead = () => { notifications.value.forEach(n => n.read = true) }
const handleNotifClick = (item) => {
  item.read = true
  router.push(item.link)
}

const userRole = computed(() => {
  const role = userStore.userInfo?.role
  const roleMap = { student: '学生', teacher: '教师', admin: '管理员' }
  return roleMap[role] || '学生'
})

const currentRole = computed(() => {
  return userStore.userInfo?.role || 'student'
})

const menuActivePath = computed(() => {
  const p = route.path
  if (p.startsWith('/resource/')) return '/resource'
  if (p.startsWith('/teacher/student/')) return '/teacher/students'
  if (p.startsWith('/teacher/students')) return '/teacher/students'
  if (p.startsWith('/teacher/plan')) return '/teacher/plan'
  if (p.startsWith('/teacher/resource')) return '/teacher/resource'
  if (p.startsWith('/teacher/notes')) return '/teacher/notes'
  if (p.startsWith('/teacher/ai')) return '/teacher/ai'
  if (p.startsWith('/teacher/')) return '/teacher/dashboard'
  if (p.startsWith('/admin/users')) return '/admin/users'
  if (p.startsWith('/admin/resources')) return '/admin/resources'
  if (p.startsWith('/admin/')) return '/admin/dashboard'
  return p
})

const currentPageName = computed(() => {
  if (route.name === 'ResourceDetail') return '资源详情'
  if (route.path === '/teacher/dashboard') return '教师首页'
  if (route.path === '/teacher/students') return '学生学情'
  if (route.path === '/teacher/plan') return '班级计划'
  if (route.path === '/teacher/resource') return '学习资料'
  if (route.path === '/teacher/notes') return '班级笔记'
  if (route.path === '/teacher/ai') return 'AI分析助手'
  if (route.name === 'TeacherStudentDetail') return '学生学情详情'
  if (route.path === '/admin/dashboard') return '管理首页'
  if (route.path === '/admin/users') return '用户管理'
  if (route.path === '/admin/resources') return '资源管理'
  const routeMap = {
    '/dashboard': '学情信息',
    '/record-manage': '学情记录',
    '/plan': '学习计划',
    '/qa': 'AI问答',
    '/resource': '学习资料',
    '/notes': '学习笔记',
    '/profile': '个人中心',
    '/settings': '设置'
  }
  return routeMap[route.path] || '首页'
})

const handleCommand = (command) => {
  switch (command) {
    case 'profile': router.push('/profile'); break
    case 'settings': router.push('/settings'); break
    case 'logout': handleLogout(); break
  }
}

if (userStore.isLoggedIn && !userStore.userInfo) {
  userStore.fetchUserInfo()
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  }).catch(() => {})
}
</script>

<style scoped>
.layout-container { height: 100vh; }
.aside { background: linear-gradient(180deg, #304156 0%, #1a1a2e 100%); display: flex; flex-direction: column; transition: width 0.3s; box-shadow: 2px 0 10px rgba(0,0,0,0.2); }
.logo { height: 70px; display: flex; align-items: center; justify-content: center; gap: 10px; padding: 0 15px; border-bottom: 1px solid rgba(255,255,255,0.1); transition: all 0.3s; }
.logo-collapse { padding: 0; justify-content: center; }
.logo-icon { color: var(--theme-color); display: flex; align-items: center; animation: float 3s ease-in-out infinite; }
.logo-text { margin: 0; font-size: 18px; font-weight: 600; color: #fff; white-space: nowrap; }
.el-menu-vertical { flex: 1; border-right: none; }
.el-menu-vertical:not(.el-menu--collapse) { width: 220px; }
:deep(.el-menu-item) { height: 50px; line-height: 50px; margin: 4px 12px; border-radius: 8px; transition: all 0.3s; font-size: 14px; }
:deep(.el-menu-item:hover) { background: var(--theme-color-light) !important; color: var(--theme-color) !important; }
:deep(.el-menu-item.is-active) { background: linear-gradient(90deg, var(--theme-color-lighter), var(--theme-color-light)) !important; color: #fff !important; font-weight: 600; }
:deep(.el-menu-item.is-active)::before { content: ''; position: absolute; left: 0; top: 50%; transform: translateY(-50%); width: 4px; height: 24px; background: var(--theme-color); border-radius: 0 4px 4px 0; }
:deep(.el-menu-item .el-icon) { font-size: 18px; margin-right: 8px; }
.collapse-btn { padding: 15px; text-align: center; color: #666; cursor: pointer; border-top: 1px solid rgba(255,255,255,0.1); transition: all 0.3s; display: flex; align-items: center; justify-content: center; gap: 8px; font-size: 13px; }
.collapse-btn:hover { color: var(--theme-color); background: rgba(255,255,255,0.05); }
.header { background: #fff; box-shadow: 0 1px 4px rgba(0,21,41,0.08); display: flex; align-items: center; justify-content: space-between; padding: 0 20px; height: 60px !important; }
.header-left { display: flex; align-items: center; }
.breadcrumb { padding-left: 10px; border-left: 3px solid var(--theme-color); }
.header-right { display: flex; align-items: center; gap: 20px; }
.header-action { cursor: pointer; padding: 8px; border-radius: 8px; transition: all 0.3s; }
.header-action:hover { background: #f5f7fa; }
.user-info { cursor: pointer; }
.user-avatar { display: flex; align-items: center; gap: 10px; padding: 5px 10px; border-radius: 8px; transition: all 0.3s; }
.user-avatar:hover { background: #f5f7fa; }
.user-detail { display: flex; flex-direction: column; }
.user-name { font-size: 14px; font-weight: 500; color: #303133; line-height: 1.3; }
.user-role { font-size: 12px; color: #909399; line-height: 1.3; }
.arrow-down { color: #909399; font-size: 12px; }
.main { background: #f0f2f5; padding: 20px; overflow-y: auto; }
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
@keyframes float { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-3px); } }
.notif-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-bottom: 1px solid #f0f0f0; }
.notif-title { font-size: 14px; font-weight: 600; color: #303133; }
.notif-empty { padding: 30px 0; text-align: center; color: #909399; font-size: 13px; }
.notif-list { max-height: 320px; overflow-y: auto; }
.notif-item { display: flex; align-items: flex-start; gap: 10px; padding: 12px 16px; cursor: pointer; transition: background 0.2s; border-bottom: 1px solid #f5f7fa; }
.notif-item:hover { background: #f5f7fa; }
.notif-item.unread { background: #ecf5ff; }
.notif-item.unread:hover { background: #e6effe; }
.notif-icon { margin-top: 2px; font-size: 18px; flex-shrink: 0; color: var(--theme-color); }
.notif-body { flex: 1; min-width: 0; }
.notif-text { margin: 0 0 4px 0; font-size: 13px; color: #303133; line-height: 1.4; }
.notif-time { font-size: 11px; color: #909399; }
.notif-dot { width: 7px; height: 7px; background: var(--theme-color); border-radius: 50%; flex-shrink: 0; margin-top: 5px; }
@media (max-width: 768px) { .user-detail, .breadcrumb { display: none; } }
</style>