<template>
  <div class="resource-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">学习资源库</h2>
        <p class="page-subtitle">精选优质资源，助力高效学习</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="showAddDialog = true">
          <el-icon><Plus /></el-icon>
          添加资源
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon gradient-blue">
            <el-icon><Collection /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ resources.length }}</span>
            <span class="stat-label">资源总数</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon gradient-green">
            <el-icon><VideoPlay /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ videoCount }}</span>
            <span class="stat-label">视频资源</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon gradient-orange">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ docCount }}</span>
            <span class="stat-label">文档资料</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon gradient-purple">
            <el-icon><Star /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ favoriteCount }}</span>
            <span class="stat-label">我的收藏</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <div class="filter-row">
        <div class="filter-group">
          <span class="filter-label">科目：</span>
          <el-radio-group v-model="activeSubject" size="default" @change="handleFilter">
            <el-radio-button label="全部" value="" />
            <el-radio-button label="数学" value="数学" />
            <el-radio-button label="语文" value="语文" />
            <el-radio-button label="英语" value="英语" />
            <el-radio-button label="物理" value="物理" />
            <el-radio-button label="化学" value="化学" />
            <el-radio-button label="历史" value="历史" />
          </el-radio-group>
        </div>
        <div class="filter-group">
          <span class="filter-label">类型：</span>
          <el-radio-group v-model="activeType" size="default" @change="handleFilter">
            <el-radio-button label="全部" value="" />
            <el-radio-button label="视频" value="视频" />
            <el-radio-button label="文档" value="文档" />
            <el-radio-button label="练习" value="练习" />
            <el-radio-button label="音频" value="音频" />
          </el-radio-group>
        </div>
      </div>
    </el-card>

    <!-- 排序和视图切换 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <span class="result-count">共找到 <strong>{{ resources.length }}</strong> 个资源</span>
      </div>
      <div class="toolbar-right">
        <el-select v-model="sortType" size="small" style="width: 120px; margin-right: 10px;">
          <el-option label="综合排序" value="default" />
          <el-option label="推荐指数" value="score" />
          <el-option label="最新资源" value="time" />
        </el-select>
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button label="grid">
            <el-icon><Grid /></el-icon>
          </el-radio-button>
          <el-radio-button label="list">
            <el-icon><List /></el-icon>
          </el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>

    <!-- 网格视图资源列表 -->
    <div v-else-if="resources.length > 0" :class="['resource-grid', viewMode]">
      <div
        v-for="(item, idx) in resources"
        :key="item.id"
        :class="['resource-card', { 'is-favorited': isFavorited(item.id) }]"
        :style="{ animationDelay: idx * 0.05 + 's' }"
        @mouseenter="hoveredId = item.id"
        @mouseleave="hoveredId = null"
      >
        <div class="card-image">
          <div :class="['type-badge', item.type]">
            <el-icon v-if="item.type === '视频'"><VideoPlay /></el-icon>
            <el-icon v-else-if="item.type === '文档'"><Document /></el-icon>
            <el-icon v-else-if="item.type === '练习'"><EditPen /></el-icon>
            <el-icon v-else><Microphone /></el-icon>
            <span>{{ item.type }}</span>
          </div>
          <div v-if="item.recommendScore >= 90" class="hot-badge">
            <el-icon><Star /></el-icon>精选
          </div>
          <div class="subject-overlay">
            <el-tag size="small" type="info">{{ item.subject || '通用' }}</el-tag>
          </div>
        </div>

        <div class="card-content">
          <h4 class="card-title">{{ item.title }}</h4>
          <p class="card-desc">{{ item.description || '暂无描述' }}</p>

          <div class="card-meta">
            <div class="meta-item">
              <el-icon><Star /></el-icon>
              <span>推荐指数：{{ item.recommendScore || 0 }}</span>
            </div>
          </div>
        </div>

        <div class="card-footer">
          <el-button
            type="primary"
            size="small"
            class="primary-btn"
            @click="openResource(item)"
          >
            <el-icon><View /></el-icon>
            查看详情
          </el-button>
          <el-button
            :type="isFavorited(item.id) ? 'warning' : 'default'"
            size="small"
            @click="toggleFavorite(item)"
          >
            <el-icon><Star /></el-icon>
            {{ isFavorited(item.id) ? '已收藏' : '收藏' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <el-empty description="暂无资源，换个条件试试吧">
        <el-button type="primary" @click="handleFilter">刷新资源</el-button>
      </el-empty>
    </div>

    <!-- 添加资源对话框 -->
    <el-dialog v-model="showAddDialog" title="添加学习资源" width="500px">
      <el-form ref="addFormRef" :model="addForm" :rules="addRules" label-width="90px">
        <el-form-item label="资源标题" prop="title">
          <el-input v-model="addForm.title" placeholder="请输入资源标题" />
        </el-form-item>
        <el-form-item label="资源类型" prop="type">
          <el-select v-model="addForm.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="视频" value="视频" />
            <el-option label="文档" value="文档" />
            <el-option label="练习" value="练习" />
            <el-option label="音频" value="音频" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属科目" prop="subject">
          <el-select v-model="addForm.subject" placeholder="请选择科目" style="width: 100%">
            <el-option label="数学" value="数学" />
            <el-option label="语文" value="语文" />
            <el-option label="英语" value="英语" />
            <el-option label="物理" value="物理" />
            <el-option label="化学" value="化学" />
            <el-option label="历史" value="历史" />
            <el-option label="通用" value="通用" />
          </el-select>
        </el-form-item>
        <el-form-item label="资源链接" prop="url">
          <el-input
            v-model="addForm.url"
            placeholder="B站请填稿件页链接，如 https://www.bilibili.com/video/BV…；其他填完整 https 链接"
          />
        </el-form-item>
        <el-form-item label="资源描述">
          <el-input v-model="addForm.description" type="textarea" :rows="3" placeholder="请输入资源描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getRecommendResources, getResourcesBySubject, getResourcesByType } from '@/api/resource'
import { ElMessage } from 'element-plus'
import {
  Plus, Collection, VideoPlay, Document, Star, Grid, List,
  View, EditPen, Microphone
} from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const resources = ref([])
const activeSubject = ref('')
const activeType = ref('')
const sortType = ref('default')
const viewMode = ref('grid')
const hoveredId = ref(null)
const showAddDialog = ref(false)
const addFormRef = ref(null)
const favoriteIds = ref([])

const addForm = ref({
  title: '',
  type: '',
  subject: '',
  url: '',
  description: ''
})

const addRules = {
  title: [{ required: true, message: '请输入资源标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择资源类型', trigger: 'change' }],
  subject: [{ required: true, message: '请选择所属科目', trigger: 'change' }]
}

const videoCount = computed(() => resources.value.filter(r => r.type === '视频').length)
const docCount = computed(() => resources.value.filter(r => r.type === '文档').length)
const favoriteCount = computed(() => favoriteIds.value.length)

const isFavorited = (id) => favoriteIds.value.includes(id)

const loadResources = async () => {
  loading.value = true
  try {
    const res = await getRecommendResources()
    if (res.code === 200) {
      resources.value = res.data || []
    }
  } catch {
    ElMessage.error('加载资源失败')
  } finally {
    loading.value = false
  }
}

const handleFilter = async () => {
  loading.value = true
  try {
    let res
    if (activeSubject.value) {
      res = await getResourcesBySubject(activeSubject.value)
    } else if (activeType.value) {
      res = await getResourcesByType(activeType.value)
    } else {
      res = await getRecommendResources()
    }
    if (res.code === 200) {
      resources.value = res.data || []
    }
  } catch {
    ElMessage.error('筛选失败')
  } finally {
    loading.value = false
  }
}

/** 进入站内资源详情页；在详情页可打开外链或复制链接 */
const openResource = (item) => {
  if (!item?.id) return
  router.push({ name: 'ResourceDetail', params: { id: String(item.id) } })
}

const toggleFavorite = (item) => {
  const id = item.id
  if (favoriteIds.value.includes(id)) {
    favoriteIds.value = favoriteIds.value.filter(fid => fid !== id)
    ElMessage.success('已取消收藏')
  } else {
    favoriteIds.value.push(id)
    ElMessage.success(`已收藏：「${item.title}」`)
  }
}

const handleAdd = async () => {
  try {
    await addFormRef.value.validate()
  } catch {
    return
  }
  ElMessage.success('资源添加成功（后端接口待开发）')
  showAddDialog.value = false
}

onMounted(() => {
  loadResources()
})
</script>

<style scoped>
.resource-container {
  padding: 10px;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #67C23A, #85ce61);
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

/* 筛选卡片 */
.filter-card {
  margin-bottom: 16px;
  border-radius: 12px;
}

.filter-row {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.filter-group {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-label {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  white-space: nowrap;
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 4px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.result-count {
  font-size: 14px;
  color: #909399;
}

.result-count strong {
  color: #409EFF;
  font-size: 16px;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

/* 加载状态 */
.loading-container {
  padding: 20px 0;
}

/* 网格视图 */
.resource-grid.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

/* 列表视图 */
.resource-grid.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.resource-grid.list .resource-card {
  flex-direction: row;
  height: auto;
}

.resource-grid.list .card-image {
  width: 160px;
  height: 100px;
  flex-shrink: 0;
}

.resource-grid.list .card-content {
  flex: 1;
  padding: 12px 16px;
}

.resource-grid.list .card-footer {
  padding: 12px 16px;
  border-top: none;
  flex-shrink: 0;
}

/* 资源卡片 */
.resource-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  animation: fadeInUp 0.5s ease both;
  display: flex;
  flex-direction: column;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.resource-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
}

.resource-card.is-favorited {
  border: 2px solid #E6A23C;
}

/* 卡片图片区域 */
.card-image {
  height: 140px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.type-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
}

.type-badge.视频 { color: #409EFF; }
.type-badge.文档 { color: #67C23A; }
.type-badge.练习 { color: #E6A23C; }
.type-badge.音频 { color: #909399; }

.hot-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  background: linear-gradient(135deg, #ff6633, #ff8855);
  color: #fff;
}

.subject-overlay {
  position: absolute;
  bottom: 12px;
  left: 12px;
}

/* 卡片内容 */
.card-content {
  flex: 1;
  padding: 16px;
}

.card-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-desc {
  margin: 0 0 12px 0;
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 3.2em;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

/* 卡片底部 */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.primary-btn {
  background: linear-gradient(135deg, #ff6633, #ff8855) !important;
  border: none !important;
}

.primary-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 102, 51, 0.4);
}

/* 空状态 */
.empty-state {
  padding: 60px 0;
  text-align: center;
}
</style>
