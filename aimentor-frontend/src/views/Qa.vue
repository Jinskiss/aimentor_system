<template>
  <div class="qa-container">

    <!-- ================== 页面标题栏 ================== -->
    <div class="qa-header">
      <div class="header-left">
        <el-icon class="header-icon" color="#409EFF"><ChatDotRound /></el-icon>
        <h2 class="header-title">AI 智能学习助手</h2>
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

    <!-- ================== 聊天卡片 ================== -->
    <el-card class="qa-card" body-style="padding: 0">

      <!-- 欢迎提示（首次打开时显示） -->
      <div v-if="messages.length === 0" class="welcome-area">
        <div class="welcome-icon">
          <el-icon :size="48" color="#409EFF"><ChatDotRound /></el-icon>
        </div>
        <h3 class="welcome-title">你好，我是 AI 学习助手</h3>
        <p class="welcome-desc">
          我可以帮你解答学习问题、解释概念、辅导作业。<br>
          试试问我：「函数是什么？」「如何理解概率论？」<br>
          我支持 Markdown 格式，回答中的代码会自动高亮显示。
        </p>
        <div class="quick-questions">
          <el-tag
            v-for="q in quickQuestions"
            :key="q"
            class="quick-tag"
            effect="plain"
            round
            @click="sendQuickQuestion(q)"
          >
            {{ q }}
          </el-tag>
        </div>
      </div>

      <!-- 消息列表 -->
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
            {{ msg.role === 'user' ? '我' : 'AI' }}
          </div>

          <div :class="['bubble-wrap', msg.role]">
            <div v-if="msg.role === 'ai'" class="markdown-body" v-html="renderMarkdown(msg.content)" />
            <div v-else class="user-content">{{ msg.content }}</div>
            <div v-if="msg.error" class="error-tip">
              <el-icon color="#F56C6C"><WarningFilled /></el-icon>
              {{ msg.error }}
            </div>
          </div>
        </div>

        <!-- AI 正在输入动画 -->
        <div v-if="aiTyping" class="message-row ai">
          <div class="avatar ai">AI</div>
          <div class="bubble-wrap ai">
            <div class="typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- ================== 输入区域 ================== -->
      <div class="input-area">
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
        <div class="input-actions">
          <span class="input-tip">Enter 发送 | Shift+Enter 换行</span>
          <el-button
            type="primary"
            :disabled="!question.trim() || loading"
            :loading="loading"
            @click="handleSend"
          >
            {{ loading ? '思考中...' : '发送' }}
          </el-button>
        </div>
      </div>

    </el-card>
  </div>
</template>

<script setup>
/**
 * Qa.vue - AI 问答页面
 *
 * 功能说明：
 *   1. 展示对话历史（用户消息 + AI 回答）
 *   2. AI 回答支持 Markdown 渲染（代码高亮、列表、粗体等）
 *   3. 多轮对话：同一会话中的问答会保持上下文（Python 服务维护历史）
 *   4. 清空对话：发送 /api/qa/reset 请求
 *   5. 快捷问题：首次打开时展示常用示例问题
 *
 * 接口说明：
 *   - POST /api/qa/ask  { question, sessionId }  -> 获取 AI 回答
 *   - POST /api/qa/reset { sessionId }            -> 清空对话历史
 */

import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Delete, WarningFilled } from '@element-plus/icons-vue'
import { askQuestion, resetConversation } from '@/api/qa'

// ================== 状态 ==================
const question = ref('')
const loading = ref(false)
const aiTyping = ref(false)
const resetting = ref(false)
const messages = ref([])
const chatHistoryRef = ref(null)
const sessionId = ref('default')

// ================== 快捷问题 ==================
const quickQuestions = [
  '什么是函数？',
  '帮我解释一下梯度下降',
  '用 Python 写一个快速排序',
  '如何理解大数定律？',
]

// ================== Markdown 渲染（纯前端实现，无需引入库）====================

/**
 * 将 Markdown 文本渲染为 HTML
 * 支持：代码块、行内代码、粗体、斜体、标题、列表、换行
 *
 * @param {string} text 原始 Markdown 文本
 * @returns {string} 渲染后的 HTML 字符串
 */
const renderMarkdown = (text) => {
  if (!text) return ''

  let html = text
    // 转义 HTML 特殊字符（防止 XSS）
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

    // 代码块：```language\n...\n```
    .replace(/```(\w*)\n([\s\S]*?)```/g, (_, lang, code) => {
      const esc = code.trim()
        .replace(/&lt;/g, '<')
        .replace(/&gt;/g, '>')
        .replace(/&amp;/g, '&')
      return '<pre class="code-block"><code>' + esc + '</code></pre>'
    })

    // 行内代码：`...`
    .replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')

    // 粗体：**...**
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')

    // 斜体：*...*（非粗体）
    .replace(/(?<!\*)\*([^*\n]+)\*(?!\*)/g, '<em>$1</em>')

    // 标题
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^# (.+)$/gm, '<h1>$1</h1>')

    // 列表
    .replace(/^[-*] (.+)$/gm, '<li>$1</li>')
    .replace(/(<li>.*<\/li>\n?)+/g, '<ul>$&</ul>')

    // 换行
    .replace(/\n\n/g, '</p><p>')
    .replace(/\n/g, '<br>')

  return '<p>' + html + '</p>'
}

// ================== 发送消息 ==================

/**
 * 发送问题给 AI
 * 流程：追加用户消息 -> 调用后端 -> 追加 AI 回答 -> 滚动到底部
 */
const handleSend = async () => {
  const q = question.value.trim()
  if (!q || loading.value) return

  messages.value.push({ role: 'user', content: q })
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
      messages.value.push({ role: 'ai', content: res.data.answer })
      console.log('[Qa] AI 回答接收成功，长度:', res.data.answer.length, '字符')
    } else {
      messages.value.push({ role: 'ai', content: '', error: res.msg || 'AI 服务异常，请稍后重试' })
      console.error('[Qa] AI 回答失败:', res.msg)
    }
  } catch (err) {
    console.error('[Qa] 发送请求失败:', err)
    messages.value.push({ role: 'ai', content: '', error: '请求失败，请检查 AI 服务是否启动' })
  } finally {
    loading.value = false
    aiTyping.value = false
    await nextTick()
    scrollToBottom()
  }
}

const sendQuickQuestion = (q) => {
  question.value = q
  handleSend()
}

/**
 * 清空对话历史
 */
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
  padding: 0 4px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 24px;
}

.header-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.qa-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 欢迎区 */
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
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #ecf5ff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.welcome-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.welcome-desc {
  margin: 0;
  font-size: 14px;
  color: #909399;
  line-height: 1.8;
  max-width: 480px;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  margin-top: 8px;
}

.quick-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.quick-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

/* 消息列表 */
.chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  scroll-behavior: smooth;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.message-row.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
  color: #fff;
}

.avatar.user { background: #409EFF; }
.avatar.ai   { background: #67C23A; }

.bubble-wrap {
  max-width: 72%;
  min-width: 60px;
}

.bubble-wrap.user { align-items: flex-end; }
.bubble-wrap.ai   { align-items: flex-start; }

.user-content {
  background: #409EFF;
  color: #fff;
  padding: 10px 14px;
  border-radius: 12px 12px 4px 12px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.markdown-body {
  background: #f5f7fa;
  color: #303133;
  padding: 12px 16px;
  border-radius: 4px 12px 12px 12px;
  font-size: 14px;
  line-height: 1.8;
  word-break: break-word;
}

/* Markdown 渲染样式 */
.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) { margin: 12px 0 6px; color: #303133; }
.markdown-body :deep(h1) { font-size: 18px; }
.markdown-body :deep(h2) { font-size: 16px; }
.markdown-body :deep(h3) { font-size: 15px; }
.markdown-body :deep(p) { margin: 8px 0; }

.markdown-body :deep(code.inline-code) {
  background: #e8f4ff;
  color: #409EFF;
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

.error-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #F56C6C;
  margin-top: 6px;
}

/* 输入动画 */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #67C23A;
  border-radius: 50%;
  animation: typing-bounce 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing-bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

/* 输入区域 */
.input-area {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.input-tip {
  font-size: 12px;
  color: #c0c4cc;
}

.input-actions .el-button {
  min-width: 80px;
}
</style>
