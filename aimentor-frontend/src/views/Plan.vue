<template>
  <div class="plan-container">

    <!-- ================== 页面标题 ================== -->
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

    <!-- ================== 统计卡片 ================== -->
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

    <!-- ================== 计划列表 ================== -->
    <el-card class="plan-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <el-icon class="card-icon"><List /></el-icon>
            <span>我的计划</span>
          </div>
          <el-select
            v-model="filterStatus"
            placeholder="筛选状态"
            size="small"
            style="width: 120px;"
            clearable
          >
            <el-option label="全部" value="" />
            <el-option label="进行中" value="未完成" />
            <el-option label="已完成" value="已完成" />
          </el-select>
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="filteredPlans.length === 0" class="empty-container">
        <el-icon class="empty-icon"><DocumentDelete /></el-icon>
        <h4>暂无学习计划</h4>
        <p>点击上方按钮，生成你的第一个学习计划吧！</p>
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

    <!-- ================== 生成计划对话框 ================== -->
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

    <!-- ================== 计划详情对话框 ================== -->
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
  Plus,
  Document,
  SuccessFilled,
  Clock,
  Calendar,
  List,
  Loading,
  DocumentDelete,
  CircleCheck,
  View,
  Delete,
  MagicStick
} from '@element-plus/icons-vue'
import {
  getMyPlans,
  generatePlan,
  completePlan,
  deletePlan
} from '@/api/plan'

// 科目选项
const subjectOptions = [
  '数学', '语文', '英语', '物理', '化学', '生物',
  '历史', '地理', '政治',
  '信息技术', '通用技术'
]

// 状态
const plans = ref([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const currentPlan = ref(null)
const filterStatus = ref('')
const formRef = ref(null)

// 表单
const form = ref({
  subject: '',
  days: 7,
  note: ''
})

// 校验规则
const rules = {
  subject: [
    { required: true, message: '请选择科目', trigger: 'change' }
  ],
  days: [
    { required: true, message: '请输入天数', trigger: 'blur' }
  ]
}

// 统计
const completedCount = computed(() => plans.value.filter(p => p.status === '已完成').length)
const inProgressCount = computed(() => plans.value.filter(p => p.status !== '已完成').length)
const uniqueSubjects = computed(() => new Set(plans.value.map(p => p.subject)).size)

// 筛选后的计划
const filteredPlans = computed(() => {
  if (!filterStatus.value) return plans.value
  return plans.value.filter(p => p.status === filterStatus.value)
})

// 加载计划列表
const loadPlans = async () => {
  loading.value = true
  try {
    const res = await getMyPlans()
    if (res.code === 200) {
      plans.value = res.data || []
    }
  } catch {
    // 已在 request 拦截器中提示具体错误
  } finally {
    loading.value = false
  }
}

// 显示生成对话框
const showGenerateDialog = () => {
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  form.value = {
    subject: '',
    days: 7,
    note: ''
  }
  formRef.value?.resetFields()
}

// 生成计划
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
      ElMessage.success('学习计划生成成功！')
      dialogVisible.value = false
      resetForm()
      await loadPlans()
    } else {
      ElMessage.error(res.msg || '生成失败')
    }
  } catch {
    // 已在 request 拦截器中提示具体错误（含业务 code≠200、HTTP 错误体 message）
  } finally {
    submitting.value = false
  }
}

// 标记完成
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
    // 已在 request 拦截器中提示
  }
}

// 从详情弹窗标记完成
const handleCompleteFromDetail = async () => {
  if (currentPlan.value) {
    await handleComplete(currentPlan.value.id)
    detailVisible.value = false
  }
}

// 查看详情
const showPlanDetail = (plan) => {
  currentPlan.value = plan
  detailVisible.value = true
}

// 删除计划
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除此学习计划吗？删除后无法恢复。', '警告', {
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
    // 已在 request 拦截器中提示
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

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #409EFF, #66b1ff);
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
  color: #409EFF !important;
  border: none !important;
  font-weight: 600;
}

.generate-btn:hover {
  background: rgba(255, 255, 255, 0.9) !important;
}

/* 统计卡片行 */
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

.stat-icon.gradient-blue { background: linear-gradient(135deg, #409EFF, #66b1ff); color: #fff; }
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

/* 计划卡片 */
.plan-card {
  border-radius: 16px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
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
  color: #409EFF;
  font-size: 20px;
}

/* 加载和空状态 */
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

/* 计划列表 */
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
  border-left: 4px solid #409EFF;
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

/* 表单提示 */
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}

/* 详情弹窗 */
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

/* 深度选择器 */
:deep(.el-card__header) {
  padding: 0;
  border-bottom: none;
}
</style>
