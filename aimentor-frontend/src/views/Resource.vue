<template>
  <div class="resource-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon-wrapper">
          <el-icon class="header-icon"><Collection /></el-icon>
        </div>
        <div class="header-text">
          <h2 class="page-title">学习资源</h2>
          <p class="page-subtitle">精选优质资源，助力学习成长</p>
        </div>
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
            <span class="stat-label">总资源数</span>
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
            <span class="stat-label">文档资源</span>
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

    <!-- 筛选卡片 -->
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
            <el-radio-button label="生物" value="生物" />
          </el-radio-group>
        </div>
        <div class="filter-group">
          <span class="filter-label">类型：</span>
          <el-radio-group v-model="activeType" size="default" @change="handleFilter">
            <el-radio-button label="全部" value="" />
            <el-radio-button label="视频" value="视频" />
            <el-radio-button label="文档" value="文档" />
            <el-radio-button label="文章" value="文章" />
            <el-radio-button label="其他" value="其他" />
          </el-radio-group>
        </div>
      </div>
    </el-card>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <span class="result-count">共 <strong>{{ resources.length }}</strong> 个资源</span>
      </div>
      <div class="toolbar-right">
        <el-select v-model="sortType" size="small" style="width: 120px; margin-right: 10px;">
          <el-option label="默认排序" value="default" />
          <el-option label="评分最高" value="score" />
          <el-option label="最新添加" value="time" />
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

    <!-- 资源列表 -->
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
          <!-- 有封面图片时显示图片，失败时显示默认图标 -->
          <img 
            v-if="item.coverImage && !imageErrors[item.id]" 
            :src="item.coverImage" 
            class="cover-img" 
            :alt="item.title"
            @error="handleImageError(item.id)"
          />
          <!-- 没有封面图片或加载失败则显示类型图标 -->
          <div v-else :class="['image-bg', item.type]">
            <el-icon v-if="item.type === '视频'" class="bg-icon"><VideoPlay /></el-icon>
            <el-icon v-else-if="item.type === '文档'" class="bg-icon"><Document /></el-icon>
            <el-icon v-else-if="item.type === '文章'" class="bg-icon"><EditPen /></el-icon>
            <el-icon v-else-if="item.type === 'exercise'" class="bg-icon"><Edit /></el-icon>
            <el-icon v-else-if="item.type === 'audio'" class="bg-icon"><Microphone /></el-icon>
            <el-icon v-else class="bg-icon"><Collection /></el-icon>
            <span class="bg-text">{{ item.title?.charAt(0) || '资' }}</span>
          </div>
          <div class="type-badge">
            <el-icon v-if="item.type === '视频'"><VideoPlay /></el-icon>
            <el-icon v-else-if="item.type === '文档'"><Document /></el-icon>
            <el-icon v-else-if="item.type === '文章'"><EditPen /></el-icon>
            <el-icon v-else><Microphone /></el-icon>
            <span>{{ item.type }}</span>
          </div>
          <div v-if="item.recommendScore >= 90" class="hot-badge">
            <el-icon><Star /></el-icon>热门
          </div>
          <div class="subject-overlay">
            <el-tag size="small" type="info">{{ item.subject || '未分类' }}</el-tag>
          </div>
        </div>

        <div class="card-content">
          <h4 class="card-title">{{ item.title }}</h4>
          <p class="card-desc">{{ item.description || '暂无描述' }}</p>

          <div class="card-meta">
            <div class="meta-item">
              <el-icon><Star /></el-icon>
              <span>评分：{{ item.recommendScore || 0 }}</span>
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
      <el-empty description="暂无学习资源">
        <el-button type="primary" @click="handleFilter">刷新列表</el-button>
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
            <el-option label="文章" value="文章" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属科目" prop="subject">
          <el-select v-model="addForm.subject" placeholder="请选择科目" style="width: 100%">
            <el-option label="数学" value="数学" />
            <el-option label="语文" value="语文" />
            <el-option label="英语" value="英语" />
            <el-option label="物理" value="物理" />
            <el-option label="化学" value="化学" />
            <el-option label="生物" value="生物" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="资源链接" prop="url">
          <el-input
            v-model="addForm.url"
            placeholder="支持B站视频或https链接，例如 https://www.bilibili.com/video/BV..."
          />
        </el-form-item>
        <el-form-item label="资源描述">
          <el-input v-model="addForm.description" type="textarea" :rows="3" placeholder="请输入资源描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">添加资源</el-button>
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
  View, EditPen, Microphone, Edit
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
const imageErrors = ref({})

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

/** 图片加载失败时隐藏图片，显示默认图标 */
const handleImageError = (id) => {
  imageErrors.value[id] = true
  console.warn('[Resource] 图片加载失败，id=', id)
}

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

/** 跳转到资源详情页 */
const openResource = (item) => {
  if (!item?.id) return
  // 始终跳转到详情页，详情页会根据链接类型决定如何展示
  router.push({ path: `/resource/${item.id}` })
}

const toggleFavorite = (item) => {
  const id = item.id
  if (favoriteIds.value.includes(id)) {
    favoriteIds.value = favoriteIds.value.filter(fid => fid !== id)
    ElMessage.success('已取消收藏')
  } else {
    favoriteIds.value.push(id)
    ElMessage.success(`已收藏：${item.title}`)
  }
}

const handleAdd = async () => {
  try {
    await addFormRef.value.validate()
  } catch {
    return
  }
  ElMessage.success('资源添加成功')
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

/* 页面头部 */
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

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon-wrapper {
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-icon {
  font-size: 28px;
  color: #fff;
}

.header-text {
  display: flex;
  flex-direction: column;
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

/* 统计卡片 */
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
.stat-icon.gradient-green { background: linear-gradient(135deg, #ff6633, #ff8855); color: #fff; }
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
  color: #ff6633;
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

/* 资源网格 */
.resource-grid.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

/* 资源列表 */
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

/* 卡片图片区 */
.card-image {
  height: 140px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.image-bg.视频 {
  background: linear-gradient(135deg, #ff6633 0%, #ff8855 100%);
}

.image-bg.文档 {
  background: linear-gradient(135deg, #909CF0 0%, #A8B4F5 100%);
}

.image-bg.文章 {
  background: linear-gradient(135deg, #E6A23C 0%, #F5D78E 100%);
}

.image-bg.其他 {
  background: linear-gradient(135deg, #909399 0%, #C0C4CC 100%);
}

.bg-icon {
  font-size: 36px;
  color: rgba(255, 255, 255, 0.9);
}

.bg-text {
  font-size: 28px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9);
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
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  color: #fff;
}

.type-badge.视频 { background: rgba(255, 102, 51, 0.85); }
.type-badge.文档 { background: rgba(144, 156, 240, 0.85); }
.type-badge.文章 { background: rgba(230, 162, 60, 0.85); }
.type-badge.其他 { background: rgba(144, 147, 153, 0.85); }

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
