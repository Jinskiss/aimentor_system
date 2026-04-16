<template>
  <div class="qa-container">

    <div class="qa-header">
      <div class="header-left">
        <div class="header-icon-wrapper">
          <el-icon class="header-icon"><ChatDotRound /></el-icon>
        </div>
        <div class="header-text">
          <h2 class="header-title">AI 智能学习助手</h2>
          <div class="header-status">
            <span class="status-dot"></span>
            <span class="status-text">在线</span>
          </div>
        </div>
      </div>
      <div class="header-right">
        <el-tag size="small" type="info" effect="plain">
          会话：{{ sessionId }}
        </el-tag>
        <el-button
          size="small"
          plain
          :disabled="messages.length === 0"
          @click="handleReset"
          :loading="resetting"
        >
          <el-icon><Delete /></el-icon>
          清空对话
        </el-button>
      </div>
    </div>

    <el-card class="qa-card" body-style="padding: 0">

      <div v-if="messages.length === 0" class="welcome-area">
        <div class="welcome-icon">
          <el-icon :size="56" style="color: var(--theme-color);"><ChatDotRound /></el-icon>
        </div>
        <h3 class="welcome-title">你好，我是 AI 学习助手</h3>
        <p class="welcome-desc">
          我可以帮你解答学习问题、解释概念、辅导作业<br>
          试试问我：「函数是什么？」「帮我分析这道物理题」<br>
          我支持 Markdown 格式，回答中的代码会自动高亮显示
        </p>
        <div class="quick-questions">
          <div class="quick-label">快捷问题</div>
          <el-tag
            v-for="q in quickQuestions"
            :key="q"
            class="quick-tag"
            effect="plain"
            round
            @click="sendQuickQuestion(q)"
          >
            <el-icon><Lightning /></el-icon>
            {{ q }}
          </el-tag>
        </div>

        <div class="feature-cards">
          <div class="feature-card">
            <div class="feature-icon">📚</div>
            <div class="feature-title">知识问答</div>
            <div class="feature-desc">解答各学科问题</div>
          </div>
          <div class="feature-card">
            <div class="feature-icon">💻</div>
            <div class="feature-title">题目辅导</div>
            <div class="feature-desc">解题思路分析</div>
          </div>
          <div class="feature-card">
            <div class="feature-icon">📝</div>
            <div class="feature-title">学习方法</div>
            <div class="feature-desc">学习技巧指导</div>
          </div>
        </div>
      </div>

      <div
        v-else
        class="chat-history"
        ref="chatHistoryRef"
      >
        <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['message-row', msg.role]"
        >
          <div :class="['avatar', msg.role]">
            <el-avatar v-if="msg.role === 'ai'" :size="36" style="background: var(--theme-color-gradient);">AI</el-avatar>
            <el-avatar v-else :size="36" style="background: var(--theme-color);">我</el-avatar>
          </div>

          <div :class="['bubble-wrap', msg.role]">
            <div class="bubble-header">
              <span class="sender-name">{{ msg.role === 'user' ? '我' : 'AI 助手' }}</span>
              <span class="message-time">{{ msg.time }}</span>
            </div>

            <div v-if="msg.role === 'ai'" class="markdown-body" v-html="renderMarkdown(msg.displayContent || msg.content)" />
            <div v-else class="user-content">{{ msg.content }}</div>

            <div v-if="msg.role === 'ai'" class="message-actions">
              <el-tooltip content="复制回答" placement="top">
                <el-button text size="small" @click="copyMessage(msg.content)">
                  <el-icon><DocumentCopy /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="重新生成" placement="top">
                <el-button text size="small" @click="regenerateMessage(index)">
                  <el-icon><RefreshRight /></el-icon>
                </el-button>
              </el-tooltip>
            </div>

            <div v-if="msg.error" class="error-tip">
              <el-icon color="#F56C6C"><WarningFilled /></el-icon>
              {{ msg.error }}
            </div>
          </div>
        </div>

        <div v-if="aiTyping" class="message-row ai">
          <div class="avatar ai">
            <el-avatar :size="36" style="background: var(--theme-color-gradient);">AI</el-avatar>
          </div>
          <div class="bubble-wrap ai">
            <div class="bubble-header">
              <span class="sender-name">AI 助手</span>
              <span class="message-time">思考中...</span>
            </div>
            <div class="typing-bubble">
              <div class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="input-area">
        <div class="input-wrapper">
          <el-input
            v-model="question"
            type="textarea"
            :rows="2"
            :autosize="{ minRows: 2, maxRows: 6 }"
            placeholder="输入你的问题，按 Enter 发送，Shift+Enter 换行..."
            :disabled="loading"
            resize="none"
            @keydown.enter.exact.prevent="handleSend"
          />
        </div>
        <div class="input-actions">
          <span class="input-tip">
            <el-icon><InfoFilled /></el-icon>
            Enter 发送 | Shift+Enter 换行
          </span>
          <div class="action-buttons">
            <el-button
              v-if="question.trim()"
              text
              @click="question = ''"
              :disabled="loading"
            >
              <el-icon><Delete /></el-icon>
              清空
            </el-button>
            <el-button
              type="primary"
              :disabled="!question.trim() || loading"
              :loading="loading"
              @click="handleSend"
              class="send-btn"
            >
              <el-icon v-if="!loading"><Promotion /></el-icon>
              {{ loading ? '思考中...' : '发送' }}
            </el-button>
          </div>
        </div>
      </div>

    </el-card>

  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Delete, WarningFilled, DocumentCopy, RefreshRight, Lightning, Promotion, InfoFilled } from '@element-plus/icons-vue'
import { askQuestion, resetConversation } from '@/api/qa'

const question = ref('')
const loading = ref(false)
const aiTyping = ref(false)
const resetting = ref(false)
const messages = ref([])
const chatHistoryRef = ref(null)
const sessionId = ref('default')
const typingInterval = ref(null)

// 获取路由参数（从Dashboard跳转过来时带的知识点）
const route = useRoute()

// 如果有知识点参数，自动发送问题
onMounted(() => {
  console.log('[Qa] AI 问答页面已挂载，当前会话 ID:', sessionId.value)
  
  // 检查是否有从Dashboard传来的知识点参数
  if (route.query.knowledge) {
    const knowledge = route.query.knowledge
    const subject = route.query.subject || ''
    const autoQuestion = subject 
      ? `请帮我学习${subject}科目的知识点"${knowledge}"，给出学习建议`
      : `请帮我学习知识点"${knowledge}"，给出学习建议`
    
    // 延迟发送，让页面先渲染完成
    setTimeout(() => {
      question.value = autoQuestion
      handleSend()
    }, 500)
  }
})

const quickQuestions = [
  '三角函数公式有哪些？',
  '帮我讲讲牛顿定律',
  '英语语法怎么学？',
  '化学反应方程式怎么配平',
]

const formatTime = (date) => {
  const now = date || new Date()
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

const renderMarkdown = (text) => {
  if (!text) return ''

  let html = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

    .replace(/```(\w*)\n([\s\S]*?)```/g, (_, lang, code) => {
      const esc = code.trim()
        .replace(/&lt;/g, '<')
        .replace(/&gt;/g, '>')
        .replace(/&amp;/g, '&')
      return '<pre class="code-block"><code>' + esc + '</code></pre>'
    })

    .replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')

    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')

    .replace(/(?<!\*)\*([^*\n]+)\*(?!\*)/g, '<em>$1</em>')

    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^# (.+)$/gm, '<h1>$1</h1>')

    .replace(/^[-*] (.+)$/gm, '<li>$1</li>')
    .replace(/(<li>.*<\/li>\n?)+/g, '<ul>$&</ul>')

    .replace(/\n\n/g, '</p><p>')
    .replace(/\n/g, '<br>')

  return '<p>' + html + '</p>'
}

const typeWriter = (index, fullText) => {
  const msg = messages.value[index]
  if (!msg) return

  let currentIndex = 0
  msg.displayContent = ''

  typingInterval.value = setInterval(() => {
    if (currentIndex < fullText.length) {
      msg.displayContent += fullText[currentIndex]
      currentIndex++
      scrollToBottom()
    } else {
      clearInterval(typingInterval.value)
      typingInterval.value = null
      scrollToBottom()
    }
  }, 20)
}

const handleSend = async () => {
  const q = question.value.trim()
  if (!q || loading.value) return

  const now = new Date()
  messages.value.push({
    role: 'user',
    content: q,
    time: formatTime(now)
  })

  question.value = ''
  loading.value = true
  aiTyping.value = true
  scrollToBottom()

  try {
    const res = await askQuestion({
      question: q,
      sessionId: sessionId.value
    })

    if (res.code === 200 || res.code === '200') {
      const aiIndex = messages.value.push({
        role: 'ai',
        content: res.data.answer,
        displayContent: '',
        time: formatTime(new Date())
      }) - 1

      aiTyping.value = false
      typeWriter(aiIndex, res.data.answer)
      console.log('[Qa] AI 回答接收成功，长度', res.data.answer.length, '字符')
    } else {
      aiTyping.value = false
      messages.value.push({
        role: 'ai',
        content: '',
        error: res.msg || 'AI 服务异常，请稍后重试',
        time: formatTime(new Date())
      })
      console.error('[Qa] AI 回答失败:', res.msg)
    }
  } catch (err) {
    console.error('[Qa] 发送请求失败', err)
    aiTyping.value = false
    messages.value.push({
      role: 'ai',
      content: '',
      error: '请求失败，请检查 AI 服务是否启动',
      time: formatTime(new Date())
    })
  } finally {
    loading.value = false
    nextTick()
    scrollToBottom()
  }
}

const sendQuickQuestion = (q) => {
  question.value = q
  handleSend()
}

const handleReset = async () => {
  resetting.value = true
  try {
    await resetConversation(sessionId.value)
    messages.value = []
    ElMessage.success('对话已清空')
    console.log('[Qa] 对话历史已清空')
  } catch (err) {
    console.error('[Qa] 清空对话失败:', err)
    ElMessage.error('清空失败，请重试')
  } finally {
    resetting.value = false
  }
}

const copyMessage = async (content) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制到剪贴板')
  } catch (err) {
    ElMessage.error('复制失败，请手动选择复制')
  }
}

const regenerateMessage = async (index) => {
  if (index > 0 && messages.value[index - 1]?.role === 'user') {
    const lastQuestion = messages.value[index - 1].content
    messages.value.splice(index, 1)
    question.value = lastQuestion
    handleSend()
  }
}

const scrollToBottom = () => {
  if (!chatHistoryRef.value) return
  chatHistoryRef.value.scrollTop = chatHistoryRef.value.scrollHeight
}

onMounted(() => {
  console.log('[Qa] AI 问答页面已挂载，当前会话 ID:', sessionId.value)
})
</script>

<style scoped>
.qa-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 10px;
  box-sizing: border-box;
}

.qa-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background: var(--theme-color-gradient);
  border-radius: 12px;
  color: #fff;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon-wrapper {
  width: 44px;
  height: 44px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-icon {
  font-size: 24px;
  color: #fff;
}

.header-text {
  display: flex;
  flex-direction: column;
}

.header-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.header-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  opacity: 0.9;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: var(--theme-color);
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-right .el-tag {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: #fff;
}

.header-right .el-button {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: #fff;
}

.header-right .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
}

.qa-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 12px;
}

.welcome-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
  gap: 16px;
}

.welcome-icon {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--theme-color-light), rgba(102, 177, 255, 0.1));
  display: flex;
  align-items: center;
  justify-content: center;
  animation: float 3s ease-in-out infinite;
}

.welcome-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.welcome-desc {
  margin: 0;
  font-size: 14px;
  color: #909399;
  line-height: 1.8;
  max-width: 500px;
}

.quick-questions {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.quick-label {
  font-size: 12px;
  color: #c0c4cc;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.quick-tag {
  cursor: pointer;
  padding: 10px 16px;
  font-size: 14px;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.quick-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px var(--theme-color-light);
  background: var(--theme-color-gradient) !important;
  color: #fff !important;
  border-color: transparent !important;
}

.feature-cards {
  display: flex;
  gap: 16px;
  margin-top: 24px;
  flex-wrap: wrap;
  justify-content: center;
}

.feature-card {
  width: 120px;
  padding: 20px 16px;
  background: #f5f7fa;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 32px;
}

.feature-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.feature-desc {
  font-size: 12px;
  color: #909399;
}

.chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  scroll-behavior: smooth;
  background: linear-gradient(180deg, #fafafa 0%, #f5f7fa 100%);
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  animation: fadeIn 0.3s ease;
}

.message-row.user {
  flex-direction: row-reverse;
}

.avatar {
  flex-shrink: 0;
}

.bubble-wrap {
  max-width: 75%;
  min-width: 80px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.bubble-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 4px;
}

.sender-name {
  font-size: 12px;
  font-weight: 500;
  color: #909399;
}

.bubble-wrap.user .sender-name {
  flex-direction: row-reverse;
}

.message-time {
  font-size: 11px;
  color: #c0c4cc;
}

.user-content {
  background: var(--theme-color-gradient);
  color: #fff;
  padding: 12px 16px;
  border-radius: 16px 16px 4px 16px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  box-shadow: 0 2px 8px var(--theme-color-light);
}

.bubble-wrap.ai .user-content {
  background: var(--theme-color-gradient);
  border-radius: 16px 16px 16px 4px;
}

.markdown-body {
  background: #fff;
  color: #303133;
  padding: 14px 18px;
  border-radius: 4px 16px 16px 16px;
  font-size: 14px;
  line-height: 1.8;
  word-break: break-word;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #f0f0f0;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) { margin: 12px 0 6px; color: #303133; }
.markdown-body :deep(h1) { font-size: 18px; }
.markdown-body :deep(h2) { font-size: 16px; }
.markdown-body :deep(h3) { font-size: 15px; }
.markdown-body :deep(p) { margin: 8px 0; }

.markdown-body :deep(code.inline-code) {
  background: var(--theme-color-light);
  color: var(--theme-color);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 13px;
}

.markdown-body :deep(pre.code-block) {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 14px 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 10px 0;
}

.markdown-body :deep(pre.code-block code) {
  background: none;
  color: inherit;
  padding: 0;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) { padding-left: 20px; margin: 8px 0; }
.markdown-body :deep(li) { margin: 4px 0; }
.markdown-body :deep(strong) { color: #303133; font-weight: 600; }
.markdown-body :deep(em) { color: #606266; }

.message-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
  padding: 0 4px;
}

.bubble-wrap:hover .message-actions {
  opacity: 1;
}

.message-actions .el-button {
  color: #909399;
  font-size: 12px;
}

.message-actions .el-button:hover {
  color: var(--theme-color);
}

.error-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #F56C6C;
  margin-top: 6px;
}

.typing-bubble {
  background: #fff;
  padding: 14px 18px;
  border-radius: 4px 16px 16px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #f0f0f0;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: var(--theme-color-gradient);
  border-radius: 50%;
  animation: typing-bounce 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing-bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

.input-area {
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.input-wrapper {
  border-radius: 12px;
  overflow: hidden;
}

.input-wrapper :deep(.el-textarea__inner) {
  border-radius: 12px;
  border: 2px solid #f0f0f0;
  padding: 12px 16px;
  font-size: 14px;
  transition: all 0.3s;
}

.input-wrapper :deep(.el-textarea__inner:focus) {
  border-color: var(--theme-color);
  box-shadow: 0 0 0 3px var(--theme-color-light);
}

.input-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.input-tip {
  font-size: 12px;
  color: #c0c4cc;
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

.send-btn {
  background: var(--theme-color-gradient) !important;
  border: none !important;
  min-width: 100px;
  font-weight: 500;
}

.send-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px var(--theme-color-light);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
