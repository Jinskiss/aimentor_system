<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2>注册</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号">
            <template #append>
              <el-button @click="handleSendCode" :loading="codeLoading" :disabled="countdown > 0">
                {{ countdown > 0 ? countdown + 's' : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="验证码">
          <el-input v-model="form.code" placeholder="请输入验证码" />
        </el-form-item>
        <!-- 新增角色选择 -->
        <el-form-item label="角色">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="学生" value="student"></el-option>
            <el-option label="教师" value="teacher"></el-option>
            <el-option label="管理员" value="admin"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading">注册</el-button>
          <el-button @click="$router.push('/login')">返回登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { sendCode } from '@/api/user'

// ✅ 修复：添加 role 字段
const form = ref({
  username: '',
  password: '',
  name: '',
  email: '',
  phone: '',
  code: '',
  role: ''  // 新增
})
const loading = ref(false)
const codeLoading = ref(false)
const countdown = ref(0)
const router = useRouter()
const userStore = useUserStore()

// 发送验证码
const handleSendCode = async () => {
  if (!form.value.phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(form.value.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }

  codeLoading.value = true
  try {
    const res = await sendCode(form.value.phone)
    if (res.code === 200 || res.code === '200') {
      ElMessage.success('验证码已发送: ' + res.data)
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      ElMessage.error(res.msg || '发送验证码失败')
    }
  } catch (err) {
    ElMessage.error(err.message || '发送验证码失败')
  } finally {
    codeLoading.value = false
  }
}

const handleRegister = async () => {
  console.log('=== handleRegister 被调用了 ===');   // 新增

  // ✅ 增强校验
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('用户名和密码为必填')
    return
  }
  if (!form.value.role) {
    ElMessage.warning('请选择角色')
    return
  }
  if (!form.value.phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  if (!form.value.code) {
    ElMessage.warning('请输入验证码')
    return
  }

  loading.value = true
  try {
    const res = await userStore.register(form.value)
    if (res.code === 200 || res.code === '200') {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.msg || '注册失败')
    }
  } catch (err) {
    console.error('注册错误详情:', err)
    ElMessage.error(err.message || '请求失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f2f5;
}
.register-card {
  width: 500px;
}
</style>