<template>
  <div class="plan-container">

    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">学习计划</h2>
        <p class="page-subtitle">制定计划，稳步前行</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="showGenerateDialog" class="generate-btn">
          <el-icon><Plus /></el-icon>
          生成新计划
        </el-button>
      </div>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon gradient-blue">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ plans.length }}</span>
            <span class="stat-label">总计划数</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon gradient-green">
            <el-icon><SuccessFilled /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ completedCount }}</span>
            <span class="stat-label">已完成</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon gradient-orange">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ inProgressCount }}</span>
            <span class="stat-label">进行中</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon gradient-purple">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ uniqueSubjects }}</span>
            <span class="stat-label">涉及科目</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card class="control-card">
      <div class="control-row">
        <div class="control-left">
          <el-select
            v-model="filterStatus"
            placeholder="状态筛选"
            size="default"
            style="width: 140px;"
            clearable
          >
            <el-option label="全部" value="" />
            <el-option label="进行中" value="未完成" />
            <el-option label="已完成" value="已完成" />
          </el-select>
          <el-select
            v-model="filterSubject"
            placeholder="科目筛选"
            size="default"
            style="width: 140px; margin-left: 10px;"
            clearable
          >
            <el-option label="全部科目" value="" />
            <el-option label="数学" value="数学" />
            <el-option label="语文" value="语文" />
            <el-option label="英语" value="英语" />
            <el-option label="物理" value="物理" />
            <el-option label="化学" value="化学" />
          </el-select>
        </div>
        <div class="control-right">
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button label="list">
              <el-icon><List /></el-icon> 列表
            </el-radio-button>
            <el-radio-button label="calendar">
              <el-icon><Calendar /></el-icon> 日历
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </el-card>

    <el-card v-if="viewMode === 'calendar'" class="calendar-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <el-icon class="card-icon orange"><Calendar /></el-icon>
            <span>计划进度</span>
          </div>
          <div class="calendar-nav">
            <el-button text size="small" @click="prevWeek">
              <el-icon><ArrowLeft /></el-icon>
            </el-button>
            <span class="calendar-period">{{ calendarPeriodLabel }}</span>
            <el-button text size="small" @click="nextWeek">
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>
      </template>

      <div class="gantt-container" ref="ganttRef">
        <div class="gantt-header">
          <div class="gantt-label">计划</div>
          <div class="gantt-days">
            <div v-for="day in weekDays" :key="day.date" :class="['gantt-day', { today: day.isToday }]">
              <span class="day-name">{{ day.dayName }}</span>
              <span class="day-date">{{ day.dateNum }}</span>
            </div>
          </div>
        </div>

        <div class="gantt-body">
          <div v-if="filteredPlans.length === 0" class="gantt-empty">
            暂无学习计划
          </div>
          <div v-else v-for="plan in filteredPlans" :key="plan.id" class="gantt-row">
            <div class="gantt-label">
              <el-tag size="small" type="info">{{ plan.subject }}</el-tag>
              <span class="plan-name">{{ plan.content.substring(0, 15) }}...</span>
            </div>
            <div class="gantt-days">
              <div v-for="day in weekDays" :key="day.date" :class="['gantt-day', { today: day.isToday }]">
                <div
                  v-if="isPlanInRange(plan, day.date)"
                  :class="['gantt-bar', plan.status === '已完成' ? 'completed' : 'in-progress']"
                  :style="{ width: '100%', height: '24px', borderRadius: '4px' }"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <el-card v-else class="plan-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <el-icon class="card-icon blue"><List /></el-icon>
            <span>我的计划</span>
          </div>
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="filteredPlans.length === 0" class="empty-container">
        <el-icon class="empty-icon"><DocumentDelete /></el-icon>
        <h4>暂无学习计划</h4>
        <p>点击上方按钮，生成你的第一个学习计划吧</p>
      </div>

      <div v-else class="plan-list">
        <div
          v-for="plan in filteredPlans"
          :key="plan.id"
          :class="['plan-item', { 'is-completed': plan.status === '已完成' }]"
        >
          <div class="plan-header">
            <div class="plan-subject">
              <el-tag size="small" type="info">{{ plan.subject }}</el-tag>
            </div>
            <div class="plan-status">
              <el-tag :type="plan.status === '已完成' ? 'success' : 'info'" effect="plain">
                {{ plan.status }}
              </el-tag>
            </div>
          </div>

          <div class="plan-content">
            <pre class="plan-text">{{ plan.content }}</pre>
          </div>

          <div class="plan-progress">
            <div class="progress-label">
              <span>完成进度</span>
              <span>{{ plan.progress || 0 }}%</span>
            </div>
            <el-progress
              :percentage="plan.progress || 0"
              :color="plan.progress >= 100 ? '#67C23A' : '#ff6633'"
              :stroke-width="8"
              :show-text="false"
            />
          </div>

          <div class="plan-footer">
            <div class="plan-date">
              <el-icon><Calendar /></el-icon>
              <span>{{ plan.startDate }} 至 {{ plan.endDate }}</span>
            </div>
            <div class="plan-actions">
              <el-button
                v-if="plan.status !== '已完成'"
                type="primary"
                size="small"
                link
                @click="handleComplete(plan.id)"
              >
                <el-icon><CircleCheck /></el-icon>
                标记完成
              </el-button>
              <el-button
                type="primary"
                size="small"
                link
                @click="showPlanDetail(plan)"
              >
                <el-icon><View /></el-icon>
                查看详情
              </el-button>
              <el-button
                type="danger"
                size="small"
                link
                @click="handleDelete(plan.id)"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      title="生成学习计划"
      width="500px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="科目" prop="subject">
          <el-select
            v-model="form.subject"
            placeholder="请选择科目"
            style="width: 100%;"
            filterable
            allow-create
          >
            <el-option
              v-for="subj in subjectOptions"
              :key="subj"
              :label="subj"
              :value="subj"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="计划天数" prop="days">
          <el-input-number
            v-model="form.days"
            :min="1"
            :max="30"
            style="width: 100%;"
          />
          <div class="form-tip">建议 7-14 天为一个完整的学习周期</div>
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="form.note"
            type="textarea"
            :rows="2"
            placeholder="添加备注（可选）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleGenerate" :loading="submitting">
          <el-icon v-if="!submitting"><MagicStick /></el-icon>
          {{ submitting ? '生成中...' : '生成计划' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="detailVisible"
      :title="currentPlan?.subject + ' 学习计划'"
      width="700px"
    >
      <div v-if="currentPlan" class="plan-detail">
        <div class="detail-header">
          <el-tag :type="currentPlan.status === '已完成' ? 'success' : 'info'" effect="plain">
            {{ currentPlan.status }}
          </el-tag>
          <span class="detail-date">
            {{ currentPlan.startDate }} 至 {{ currentPlan.endDate }}
          </span>
        </div>
        <div class="detail-progress">
          <div class="progress-label">
            <span>完成进度</span>
            <span>{{ currentPlan.progress || 0 }}%</span>
          </div>
          <el-progress
            :percentage="currentPlan.progress || 0"
            :color="currentPlan.progress >= 100 ? '#67C23A' : '#ff6633'"
            :stroke-width="12"
          />
        </div>
        <div class="detail-content">
          <pre>{{ currentPlan.content }}</pre>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="currentPlan && currentPlan.status !== '已完成'"
          type="primary"
          @click="handleCompleteFromDetail"
        >
          标记完成
        </el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Document, SuccessFilled, Clock, Calendar, List, Loading,
  DocumentDelete, CircleCheck, View, Delete, MagicStick,
  ArrowLeft, ArrowRight
} from '@element-plus/icons-vue'
import {
  getMyPlans,
  generatePlan,
  completePlan,
  deletePlan
} from '@/api/plan'

const subjectOptions = [
  '数学', '语文', '英语', '物理', '化学', '生物',
  '历史', '地理', '政治',
  '信息技术', '通用技术'
]

const plans = ref([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const currentPlan = ref(null)
const filterStatus = ref('')
const filterSubject = ref('')
const viewMode = ref('list')
const formRef = ref(null)
const ganttRef = ref(null)

const currentWeekStart = ref(getWeekStart(new Date()))

function getWeekStart(date) {
  const d = new Date(date)
  const day = d.getDay()
  const diff = d.getDate() - day + (day === 0 ? -6 : 1)
  return new Date(d.setDate(diff))
}

function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function addDays(date, days) {
  const result = new Date(date)
  result.setDate(result.getDate() + days)
  return result
}

const weekDays = computed(() => {
  const days = []
  const dayNames = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const today = formatDate(new Date())

  for (let i = 0; i < 7; i++) {
    const d = addDays(currentWeekStart.value, i)
    const dateStr = formatDate(d)
    days.push({
      date: dateStr,
      dayName: dayNames[i],
      dateNum: d.getDate(),
      isToday: dateStr === today
    })
  }
  return days
})

const calendarPeriodLabel = computed(() => {
  const start = currentWeekStart.value
  const end = addDays(start, 6)
  const startStr = `${start.getMonth() + 1}/${start.getDate()}`
  const endStr = `${end.getMonth() + 1}/${end.getDate()}`
  return `${startStr} - ${endStr}`
})

const prevWeek = () => {
  currentWeekStart.value = addDays(currentWeekStart.value, -7)
}

const nextWeek = () => {
  currentWeekStart.value = addDays(currentWeekStart.value, 7)
}

const isPlanInRange = (plan, date) => {
  if (!plan.startDate || !plan.endDate) return false
  return date >= plan.startDate && date <= plan.endDate
}

const form = ref({
  subject: '',
  days: 7,
  note: ''
})

const rules = {
  subject: [
    { required: true, message: '请选择科目', trigger: 'change' }
  ],
  days: [
    { required: true, message: '请输入天数', trigger: 'blur' }
  ]
}

const completedCount = computed(() => plans.value.filter(p => p.status === '已完成').length)
const inProgressCount = computed(() => plans.value.filter(p => p.status !== '已完成').length)
const uniqueSubjects = computed(() => new Set(plans.value.map(p => p.subject)).size)

const filteredPlans = computed(() => {
  let result = plans.value
  if (filterStatus.value) {
    result = result.filter(p => p.status === filterStatus.value)
  }
  if (filterSubject.value) {
    result = result.filter(p => p.subject === filterSubject.value)
  }
  return result
})

const loadPlans = async () => {
  loading.value = true
  try {
    const res = await getMyPlans()
    if (res.code === 200) {
      plans.value = res.data || []
    }
  } catch {
  } finally {
    loading.value = false
  }
}

const showGenerateDialog = () => {
  dialogVisible.value = true
}

const resetForm = () => {
  form.value = {
    subject: '',
    days: 7,
    note: ''
  }
  formRef.value?.resetFields()
}

const handleGenerate = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const res = await generatePlan({
      subject: form.value.subject,
      days: form.value.days
    })
    if (res.code === 200) {
      ElMessage.success('学习计划生成成功')
      dialogVisible.value = false
      resetForm()
      await loadPlans()
    } else {
      ElMessage.error(res.msg || '生成失败')
    }
  } catch {
  } finally {
    submitting.value = false
  }
}

const handleComplete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要将此计划标记为已完成吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
  } catch {
    return
  }

  try {
    const res = await completePlan(id)
    if (res.code === 200) {
      ElMessage.success('已标记完成')
      await loadPlans()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch {
  }
}

const handleCompleteFromDetail = async () => {
  if (currentPlan.value) {
    await handleComplete(currentPlan.value.id)
    detailVisible.value = false
  }
}

const showPlanDetail = (plan) => {
  currentPlan.value = plan
  detailVisible.value = true
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除此学习计划吗？删除后无法恢复', '警告', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    const res = await deletePlan(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadPlans()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch {
  }
}

onMounted(() => {
  loadPlans()
})
</script>

<style scoped>
.plan-container {
  padding: 10px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #ff6633, #ff8855);
  border-radius: 16px;
  color: #fff;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.page-subtitle {
  margin: 4px 0 0 0;
  font-size: 14px;
  opacity: 0.9;
}

.generate-btn {
  background: #fff !important;
  color: #ff6633 !important;
  border: none !important;
  font-weight: 600;
}

.generate-btn:hover {
  background: rgba(255, 255, 255, 0.9) !important;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.stat-icon.gradient-blue { background: linear-gradient(135deg, #ff6633, #ff8855); color: #fff; }
.stat-icon.gradient-green { background: linear-gradient(135deg, #67C23A, #85ce61); color: #fff; }
.stat-icon.gradient-orange { background: linear-gradient(135deg, #909CF0, #A8B4F5); color: #fff; }
.stat-icon.gradient-purple { background: linear-gradient(135deg, #9370DB, #ba8fdb); color: #fff; }

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

.control-card {
  margin-bottom: 16px;
  border-radius: 12px;
}

.control-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.control-left {
  display: flex;
  align-items: center;
}

.control-right {
  display: flex;
  align-items: center;
}

.calendar-card {
  margin-bottom: 16px;
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-icon {
  font-size: 20px;
}

.card-icon.blue { color: #ff6633; }
.card-icon.orange { color: #909CF0; }

.calendar-nav {
  display: flex;
  align-items: center;
  gap: 12px;
}

.calendar-period {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  min-width: 120px;
  text-align: center;
}

.gantt-container {
  padding: 16px;
}

.gantt-header {
  display: flex;
  border-bottom: 2px solid #e4e7ed;
  padding-bottom: 12px;
  margin-bottom: 8px;
}

.gantt-label {
  width: 180px;
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  padding-right: 12px;
}

.gantt-days {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.gantt-day {
  text-align: center;
  padding: 4px;
  border-radius: 4px;
}

.gantt-day.today {
  background: rgba(255, 102, 51, 0.1);
}

.day-name {
  display: block;
  font-size: 12px;
  color: #909399;
}

.day-date {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.gantt-body {
  max-height: 400px;
  overflow-y: auto;
}

.gantt-empty {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.gantt-row {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.gantt-row:last-child {
  border-bottom: none;
}

.gantt-bar {
  transition: all 0.3s;
}

.gantt-bar.in-progress {
  background: linear-gradient(135deg, #ff6633, #ff8855);
}

.gantt-bar.completed {
  background: linear-gradient(135deg, #67C23A, #85ce61);
}

.plan-name {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  display: block;
}

.plan-card {
  border-radius: 16px;
  overflow: hidden;
}

.plan-card :deep(.el-card__header) {
  padding: 0;
  border-bottom: none;
}

.loading-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #909399;
}

.loading-icon {
  font-size: 32px;
  animation: rotate 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.empty-icon {
  font-size: 64px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

.plan-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 10px 0;
}

.plan-item {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 16px 20px;
  border-left: 4px solid #ff6633;
  transition: all 0.3s;
}

.plan-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateX(4px);
}

.plan-item.is-completed {
  border-left-color: #67C23A;
  opacity: 0.8;
}

.plan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.plan-content {
  margin-bottom: 12px;
}

.plan-text {
  font-family: inherit;
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-word;
  margin: 0;
}

.plan-progress {
  margin-bottom: 12px;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #606266;
  margin-bottom: 8px;
}

.plan-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px dashed #e4e7ed;
}

.plan-date {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
}

.plan-actions {
  display: flex;
  gap: 8px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}

.plan-detail {
  padding: 10px 0;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.detail-date {
  font-size: 14px;
  color: #909399;
}

.detail-progress {
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.detail-progress .progress-label {
  margin-bottom: 8px;
}

.detail-content {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
}

.detail-content pre {
  font-family: inherit;
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-word;
  margin: 0;
}
</style>
