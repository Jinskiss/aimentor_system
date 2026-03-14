<template>
  <div class="qa">
    <el-card style="height: 600px; display: flex; flex-direction: column;">
      <div class="chat-history" ref="chatHistory">
        <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role]">
          <div class="avatar">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
          <div class="bubble">{{ msg.content }}</div>
        </div>
      </div>
      <div class="input-area">
        <el-input
          v-model="question"
          placeholder="输入你的问题..."
          @keyup.enter="sendQuestion"
          :disabled="loading"
        />
        <el-button type="primary" @click="sendQuestion" :loading="loading">发送</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { askQuestion } from '@/api/qa'
import { ElMessage } from 'element-plus'

const question = ref('')
const loading = ref(false)
const messages = ref([]) // { role: 'user' or 'ai', content: '' }
const chatHistory = ref(null)

const sendQuestion = async () => {
  if (!question.value.trim()) return
  const q = question.value
  messages.value.push({ role: 'user', content: q })
  question.value = ''
  loading.value = true
  try {
    const res = await askQuestion({ question: q })
    if (res.code === 200) {
      messages.value.push({ role: 'ai', content: res.data.answer })
    } else {
      ElMessage.error(res.msg)
    }
  } catch (err) {
    ElMessage.error('请求失败')
  } finally {
    loading.value = false
    nextTick(() => {
      chatHistory.value.scrollTop = chatHistory.value.scrollHeight
    })
  }
}
</script>

<style scoped>
.qa {
  height: 100%;
}
.chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #eee;
  margin-bottom: 10px;
}
.message {
  display: flex;
  margin-bottom: 15px;
}
.message.user {
  justify-content: flex-end;
}
.message.ai {
  justify-content: flex-start;
}
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 10px;
  flex-shrink: 0;
}
.bubble {
  max-width: 60%;
  padding: 10px;
  border-radius: 8px;
  background-color: #f4f4f5;
}
.message.user .bubble {
  background-color: #409EFF;
  color: white;
}
.input-area {
  display: flex;
  gap: 10px;
}
</style>