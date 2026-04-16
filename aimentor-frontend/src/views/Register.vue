<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2>用户注册</h2>
      <el-form :model="form" label-width="80px">
        <!-- 头像上传 -->
        <el-form-item label="头像">
          <div class="avatar-upload">
            <el-avatar :size="80" :src="avatarPreview" class="avatar-preview">
              <el-icon :size="40"><User /></el-icon>
            </el-avatar>
            <div class="upload-tip">
              <el-upload
                action="#"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="handleAvatarUpload"
              >
                <el-button size="small" type="primary">选择图片</el-button>
              </el-upload>
              <span class="tip-text">支持 JPG、PNG，建议 200x200</span>
            </div>
          </div>
        </el-form-item>

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
        <el-form-item label="手机">
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
        <!-- 角色选择 -->
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
import { User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { sendCode, uploadAvatar } from '@/api/user'

const form = ref({
  username: '',
  password: '',
  name: '',
  email: '',
  phone: '',
  code: '',
  role: ''
})
const loading = ref(false)
const codeLoading = ref(false)
const countdown = ref(0)
const router = useRouter()
const userStore = useUserStore()
const avatarPreview = ref('')
const avatarUrl = ref('')

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif' || file.type === 'image/webp'
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isJPG) {
    ElMessage.error('只能上传 JPG、PNG、GIF、WebP 格式的图片')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  // 预览
  avatarPreview.value = URL.createObjectURL(file)
  return true
}

const handleAvatarUpload = async ({ file }) => {
  try {
    const res = await uploadAvatar(file)
    if (res.code === 200 || res.code === '200') {
      avatarUrl.value = res.data
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(res.msg || '头像上传失败')
    }
  } catch (err) {
    console.error('头像上传失败:', err)
    ElMessage.error('头像上传失败')
  }
}

const handleSendCode = async () => {
  if (!form.value.phone) {
    ElMessage.warning('??????')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(form.value.phone)) {
    ElMessage.warning('?????????')
    return
  }

  codeLoading.value = true
  try {
    const res = await sendCode(form.value.phone)
    if (res.code === 200 || res.code === '200') {
      ElMessage.success('??????: ' + res.data)
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      ElMessage.error(res.msg || '????')
    }
  } catch (err) {
    ElMessage.error(err.message || '????')
  } finally {
    codeLoading.value = false
  }
}

const handleRegister = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
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
    // 如果已上传头像，添加到表单
    const registerData = { ...form.value }
    if (avatarUrl.value) {
      registerData.avatar = avatarUrl.value
    }
    const res = await userStore.register(registerData)
    if (res.code === 200 || res.code === '200') {
      ElMessage.success('注册成功')
      router.push('/login')
    } else {
      ElMessage.error(res.msg || '注册失败')
    }
  } catch (err) {
    console.error('注册失败:', err)
    ElMessage.error(err.message || '注册失败')
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
.avatar-upload {
  display: flex;
  align-items: center;
  gap: 16px;
}
.avatar-preview {
  border: 2px dashed #dcdfe6;
  background: #f5f7fa;
}
.upload-tip {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.tip-text {
  font-size: 12px;
  color: #909399;
}
</style>