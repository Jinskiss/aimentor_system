<template>
  <div class="dashboard">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">学情分析仪表盘</h2>
        <p class="page-subtitle">全面了解你的学习状况</p>
      </div>
      <div class="header-right">
        <div class="stat-card mini">
          <div class="stat-icon blue">
            <el-icon><DataLine /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ totalScore || '--' }}</span>
            <span class="stat-label">平均成绩</span>
          </div>
        </div>
        <div class="stat-card mini">
          <div class="stat-icon orange">
            <el-icon><Star /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ weakPoints.length }}</span>
            <span class="stat-label">薄弱知识点</span>
          </div>
        </div>
      </div>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon gradient-blue">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ examCount || 0 }}</span>
            <span class="stat-label">考试次数</span>
          </div>
          <div class="stat-trend up">
            <el-icon><Top /></el-icon>
            <span>较上周</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon gradient-green">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ masteryCount || 0 }}</span>
            <span class="stat-label">已掌握知识点</span>
          </div>
          <div class="stat-trend up">
            <el-icon><Top /></el-icon>
            <span>学习计划</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon gradient-orange">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ completedPlans || 0 }}</span>
            <span class="stat-label">已完成计划</span>
          </div>
          <div class="stat-trend">
            <span>持续进步</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon class="card-icon orange"><DataLine /></el-icon>
                <span>成绩趋势</span>
              </div>
            </div>
          </template>
          <div ref="trendChartRef" style="width: 100%; height: 320px;"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon class="card-icon blue"><Histogram /></el-icon>
                <span>知识点掌握度</span>
              </div>
            </div>
          </template>
          <div ref="weakChartRef" style="width: 100%; height: 320px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon class="card-icon purple"><Odometer /></el-icon>
                <span>学科能力雷达图</span>
              </div>
            </div>
          </template>
          <div ref="radarChartRef" style="width: 100%; height: 320px;"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon class="card-icon green"><PieChart /></el-icon>
                <span>成绩分布</span>
              </div>
            </div>
          </template>
          <div ref="pieChartRef" style="width: 100%; height: 320px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card class="weak-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon class="card-icon red"><Warning /></el-icon>
                <span>需要加强的知识点</span>
              </div>
              <el-tag v-if="weakPoints.length > 0" type="info" effect="plain" size="small">
                {{ weakPoints.length }} 个待提升
              </el-tag>
            </div>
          </template>

          <div v-if="loading" class="loading-container">
            <el-icon class="loading-icon"><Loading /></el-icon>
            <span>正在加载学情数据...</span>
          </div>

          <div v-else-if="weakPoints.length > 0" class="weak-points-container">
            <el-row :gutter="15">
              <el-col
                v-for="(item, idx) in weakPoints"
                :key="item.knowledge"
                :xs="24"
                :sm="12"
                :md="8"
                :lg="6"
              >
                <div class="weak-point-item" :style="{ animationDelay: idx * 0.05 + 's' }">
                  <div class="weak-header">
                    <span class="weak-index">{{ idx + 1 }}</span>
                    <span class="weakname">{{ item.knowledge }}</span>
                  </div>
                  <div class="weak-progress">
                    <div class="progress-info">
                      <span>掌握度</span>
                      <span class="progress-value" :style="{ color: getMasteryColor(item.mastery) }">
                        {{ item.mastery }}%
                      </span>
                    </div>
                    <el-progress
                      :percentage="item.mastery"
                      :color="getMasteryColor(item.mastery)"
                      :stroke-width="10"
                      :show-text="false"
                    />
                  </div>
                  <div class="weak-footer">
                    <el-tag :type="getMasteryType(item.mastery)" size="small">
                      {{ getMasteryLabel(item.mastery) }}
                    </el-tag>
                    <el-button type="primary" link size="small" @click="goToStudy(item)">
                      去学习<el-icon><ArrowRight /></el-icon>
                    </el-button>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>

          <div v-else class="empty-container">
            <el-icon class="empty-icon"><CircleCheck /></el-icon>
            <h4>太棒了！</h4>
            <p>暂无薄弱知识点，继续保持！</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getScoreTrend, getWeakPoints, getKnowledgeMasteries } from '@/api/academic'
import { getMyPlans } from '@/api/plan'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  DataLine,
  Star,
  TrendCharts,
  CircleCheck,
  Timer,
  Medal,
  Histogram,
  Odometer,
  PieChart,
  Warning,
  Loading,
  ArrowRight,
  Top
} from '@element-plus/icons-vue'

const trendChartRef = ref(null)
const weakChartRef = ref(null)
const radarChartRef = ref(null)
const pieChartRef = ref(null)

const weakPoints = ref([])
const loading = ref(false)
const totalScore = ref(0)
const examCount = ref(0)
const masteryCount = ref(0)
const completedPlans = ref(0)
let trendChart = null
let weakChart = null
let radarChart = null
let pieChart = null

const userStore = useUserStore()
const router = useRouter()

const goToStudy = (item) => {
  router.push({
    path: '/qa',
    query: { knowledge: item.knowledge, subject: item.subject }
  })
}

const getMasteryType = (mastery) => {
  if (mastery < 30) return 'info'
  if (mastery < 60) return 'info'
  return 'success'
}

const getMasteryColor = (mastery) => {
  if (mastery < 30) return '#B4A0F0'
  if (mastery < 60) return '#909CF0'
  return '#ff6633'
}

const getMasteryLabel = (mastery) => {
  if (mastery < 30) return '薄弱'
  if (mastery < 60) return '待提升'
  return '良好'
}

const initTrendChart = (data) => {
  if (!trendChartRef.value) return

  if (trendChart) {
    trendChart.dispose()
    trendChart = null
  }

  trendChart = echarts.init(trendChartRef.value)

  const option = {
    title: { text: '成绩趋势', left: 'center', top: 10 },
    tooltip: { trigger: 'axis', formatter: '{b}<br/>分数: {c}分' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(item => item.dateMonth),
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#999' } }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLine: { lineStyle: { color: '#999' } },
      splitLine: { lineStyle: { color: '#eee' } }
    },
    series: [{
      data: data.map(item => item.avgScore),
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { color: '#ff6633', width: 3 },
      itemStyle: { color: '#ff6633', borderWidth: 2, borderColor: '#fff' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(255, 102, 51, 0.3)' },
          { offset: 1, color: 'rgba(255, 102, 51, 0.05)' }
        ])
      }
    }]
  }

  trendChart.setOption(option)
}

const initWeakChart = (data) => {
  if (!weakChartRef.value) return

  if (weakChart) {
    weakChart.dispose()
    weakChart = null
  }

  weakChart = echarts.init(weakChartRef.value)

  const barData = data.map(item => ({
    value: item.mastery,
    itemStyle: { color: item.mastery < 60 ? '#909CF0' : '#ff6633' }
  }))

  const option = {
    title: { text: '知识点掌握度', left: 'center', top: 10 },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: '{b}<br/>掌握度: {c}%' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(item => item.knowledge),
      axisLine: { lineStyle: { color: '#999' } },
      axisLabel: { interval: 0, rotate: data.length > 5 ? 30 : 0 }
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: { formatter: '{value}%' },
      splitLine: { lineStyle: { color: '#eee' } }
    },
    series: [{
      data: barData,
      type: 'bar',
      barWidth: '50%',
      label: { show: true, position: 'top', formatter: '{c}%' }
    }]
  }

  weakChart.setOption(option)
}

const initRadarChart = (data) => {
  if (!radarChartRef.value) return

  if (radarChart) {
    radarChart.dispose()
    radarChart = null
  }

  radarChart = echarts.init(radarChartRef.value)

  const subjects = [...new Set(data.map(item => item.subject || '其他'))]
  const radarData = subjects.map(subject => ({
    subject,
    value: data.filter(item => (item.subject || '其他') === subject)
      .reduce((sum, item) => sum + item.mastery, 0) /
      (data.filter(item => (item.subject || '其他') === subject).length || 1)
  }))

  const option = {
    tooltip: { trigger: 'item' },
    radar: {
      indicator: radarData.map(item => ({ name: item.subject, max: 100 })),
      radius: '65%',
      axisName: { color: '#666' }
    },
    series: [{
      type: 'radar',
      data: [{
        value: radarData.map(item => item.value),
        name: '学科能力',
        areaStyle: { color: 'rgba(147, 112, 219, 0.3)' },
        lineStyle: { color: '#9370DB' },
        itemStyle: { color: '#9370DB' }
      }]
    }]
  }

  radarChart.setOption(option)
}

const initPieChart = (data) => {
  if (!pieChartRef.value) return

  if (pieChart) {
    pieChart.dispose()
    pieChart = null
  }

  pieChart = echarts.init(pieChartRef.value)

  const excellent = data.filter(item => item.mastery >= 80).length
  const good = data.filter(item => item.mastery >= 60 && item.mastery < 80).length
  const fair = data.filter(item => item.mastery >= 30 && item.mastery < 60).length
  const poor = data.filter(item => item.mastery < 30).length

  const option = {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: '5%', left: 'center' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: {
        label: { show: true, fontSize: 16, fontWeight: 'bold' }
      },
      labelLine: { show: false },
      data: [
        { value: excellent, name: '优秀(>=80)', itemStyle: { color: '#ff6633' } },
        { value: good, name: '良好(60-80)', itemStyle: { color: '#ff6633' } },
        { value: fair, name: '一般(30-60)', itemStyle: { color: '#909CF0' } },
        { value: poor, name: '薄弱(<30)', itemStyle: { color: '#B4A0F0' } }
      ]
    }]
  }

  pieChart.setOption(option)
}

const handleResize = () => {
  trendChart?.resize()
  weakChart?.resize()
  radarChart?.resize()
  pieChart?.resize()
}

onMounted(async () => {
  loading.value = true

  try {
    if (userStore.isLoggedIn && !userStore.userInfo) {
      await userStore.fetchUserInfo()
    }
    if (!userStore.isLoggedIn) {
      ElMessage.error('请先登录')
      return
    }

    getMyPlans().then(res => {
      if (res?.code === 200 || res?.code === '200') {
        completedPlans.value = (res.data || []).filter(p => p.status === '已完成').length
      }
    }).catch(() => {})

    const [trendRes, weakRes, allMasteryRes] = await Promise.all([
      getScoreTrend(),
      getWeakPoints(),
      getKnowledgeMasteries()
    ])

    await nextTick()

    if (trendRes.code === 200 || trendRes.code === '200') {
      const trendData = trendRes.data || []
      if (trendData.length > 0) {
        initTrendChart(trendData)
        totalScore.value = Math.round(trendData.reduce((sum, item) => sum + item.avgScore, 0) / trendData.length)
        examCount.value = trendData.length
      } else {
        trendChartRef.value && (trendChartRef.value.innerHTML = '<div style="text-align:center;padding-top:100px;color:#999;">暂无成绩数据</div>')
      }
    }

    if (allMasteryRes.code === 200 || allMasteryRes.code === '200') {
      const masteryList = allMasteryRes.data || []
      if (masteryList.length > 0) {
        initWeakChart(masteryList)
        initRadarChart(masteryList)
        initPieChart(masteryList)
        masteryCount.value = masteryList.filter(item => item.mastery >= 60).length
      } else {
        weakChartRef.value && (weakChartRef.value.innerHTML = '<div style="text-align:center;padding-top:100px;color:#999;">暂无知识点数据</div>')
      }
    }

    if (weakRes.code === 200 || weakRes.code === '200') {
      weakPoints.value = weakRes.data || []
    }

  } catch {
  } finally {
    loading.value = false
  }

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  weakChart?.dispose()
  radarChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped>
.dashboard {
  min-height: 100%;
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

.header-right {
  display: flex;
  gap: 16px;
}

.stat-card.mini {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.2);
  padding: 12px 16px;
  border-radius: 12px;
}

.stat-icon.blue { background: rgba(255, 102, 51, 0.2); color: #ff6633; }
.stat-icon.orange { background: rgba(144, 156, 240, 0.2); color: #909CF0; }
.stat-icon.green { background: rgba(255, 102, 51, 0.2); color: #ff6633; }
.stat-icon.purple { background: rgba(147, 112, 219, 0.2); color: #9370DB; }

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
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

.stat-trend {
  position: absolute;
  top: 16px;
  right: 16px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #ff6633;
}

.stat-trend.up {
  color: #ff6633;
}

.chart-card, .weak-card {
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s;
}

.chart-card:hover, .weak-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
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
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.card-icon.orange { background: rgba(144, 156, 240, 0.1); color: #909CF0; }
.card-icon.blue { background: rgba(255, 102, 51, 0.1); color: #ff6633; }
.card-icon.purple { background: rgba(147, 112, 219, 0.1); color: #9370DB; }
.card-icon.green { background: rgba(255, 102, 51, 0.1); color: #ff6633; }
.card-icon.red { background: rgba(180, 160, 240, 0.1); color: #B4A0F0; }

.loading-container, .empty-container {
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
  color: #ff6633;
  margin-bottom: 16px;
}

.weak-points-container {
  padding: 10px 0;
}

.weak-point-item {
  background: linear-gradient(135deg, #fff 0%, #f5f7fa 100%);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 15px;
  transition: all 0.3s;
  animation: fadeInUp 0.5s ease both;
  border: 1px solid #f0f0f0;
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

.weak-point-item:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  transform: translateY(-4px);
  border-color: #ff6633;
}

.weak-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.weak-index {
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #ff6633, #ff8855);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.weak-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.weak-progress {
  margin-bottom: 12px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.progress-value {
  font-weight: 600;
}

.weak-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.el-card__body) {
  padding: 16px;
}

:deep(.el-progress-bar__outer) {
  background-color: #f0f0f0 !important;
}
</style>
