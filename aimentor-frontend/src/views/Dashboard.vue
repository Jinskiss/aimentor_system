<template>
  <div class="dashboard">
    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <div ref="trendChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div ref="weakChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 薄弱知识点区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>薄弱知识点</span>
              <el-tag v-if="weakPoints.length > 0" type="info" size="small">
                掌握度低于60%
              </el-tag>
            </div>
          </template>
          
          <!-- 加载状态 -->
          <div v-if="loading" class="loading-text">加载中...</div>
          
          <!-- 数据展示 -->
          <div v-else-if="weakPoints.length > 0" class="weak-points-container">
            <el-row :gutter="15">
              <el-col 
                v-for="item in weakPoints" 
                :key="item.knowledge"
                :xs="24"
                :sm="12"
                :md="8"
                :lg="6"
              >
                <div class="weak-point-item">
                  <div class="point-info">
                    <span class="point-name">{{ item.knowledge }}</span>
                    <el-tag :type="getMasteryType(item.mastery)" size="small">
                      {{ item.mastery }}分
                    </el-tag>
                  </div>
                  <el-progress 
                    :percentage="item.mastery" 
                    :color="getMasteryColor(item.mastery)"
                    :stroke-width="8"
                    :show-text="false"
                  />
                  <div class="point-action">
                    <el-button type="primary" link size="small">
                      去练习 →
                    </el-button>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
          
          <!-- 空状态 -->
          <el-empty v-else description="暂无薄弱知识点，继续保持！" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getScoreTrend, getWeakPoints } from '@/api/academic'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 图表DOM引用
const trendChartRef = ref(null)
const weakChartRef = ref(null)

// 数据
const weakPoints = ref([])
const loading = ref(false)

// 图表实例
let trendChart = null
let weakChart = null

const userStore = useUserStore()

// 辅助方法
const getMasteryType = (mastery) => {
  if (mastery < 30) return 'danger'
  if (mastery < 60) return 'warning'
  return 'success'
}

const getMasteryColor = (mastery) => {
  if (mastery < 30) return '#F56C6C'
  if (mastery < 60) return '#E6A23C'
  return '#67C23A'
}

// 初始化成绩趋势图表
const initTrendChart = (data) => {
  console.log('初始化趋势图表，数据:', data)
  
  if (!trendChartRef.value) {
    console.error('趋势图表DOM不存在')
    return
  }
  
  // 如果已有实例，先销毁
  if (trendChart) {
    trendChart.dispose()
  }
  
  trendChart = echarts.init(trendChartRef.value)
  
  const option = {
    title: { 
      text: '成绩趋势',
      left: 'center',
      top: 10
    },
    tooltip: { 
      trigger: 'axis',
      formatter: '{b}<br/>分数: {c}分'
    },
    grid: { 
      left: '3%', 
      right: '4%', 
      bottom: '3%', 
      top: '15%',
      containLabel: true 
    },
    xAxis: { 
      type: 'category', 
      // 修改：使用 dateMonth 字段
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
      // 修改：使用 avgScore 字段
      data: data.map(item => item.avgScore), 
      type: 'line', 
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { color: '#409EFF', width: 3 },
      itemStyle: { color: '#409EFF', borderWidth: 2, borderColor: '#fff' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
        ])
      }
    }]
  }
  
  trendChart.setOption(option)
  console.log('趋势图表已渲染')
}

// 初始化知识点掌握度图表
const initWeakChart = (data) => {
  console.log('初始化掌握度图表，数据:', data)
  
  if (!weakChartRef.value) {
    console.error('掌握度图表DOM不存在')
    return
  }
  
  // 如果已有实例，先销毁
  if (weakChart) {
    weakChart.dispose()
  }
  
  weakChart = echarts.init(weakChartRef.value)
  
  const option = {
    title: { 
      text: '知识点掌握度',
      left: 'center',
      top: 10
    },
    tooltip: { 
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: '{b}<br/>掌握度: {c}%'
    },
    grid: { 
      left: '3%', 
      right: '4%', 
      bottom: '3%', 
      top: '15%',
      containLabel: true 
    },
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
      data: data.map(item => ({
        value: item.mastery,
        itemStyle: {
          color: item.mastery < 60 ? '#F56C6C' : '#67C23A'
        }
      })), 
      type: 'bar',
      barWidth: '50%',
      label: {
        show: true,
        position: 'top',
        formatter: '{c}%'
      }
    }]
  }
  
  weakChart.setOption(option)
  console.log('掌握度图表已渲染')
}

// 窗口大小改变时重绘图表
const handleResize = () => {
  trendChart?.resize()
  weakChart?.resize()
}

onMounted(async () => {
  console.log('Dashboard mounted')
  loading.value = true
  
  try {
    if (userStore.isLoggedIn && !userStore.userInfo) {
      await userStore.fetchUserInfo()
    }
    const studentId = userStore.userInfo?.id
    if (studentId == null) {
      ElMessage.error('无法获取当前用户，请重新登录')
      return
    }

    const [trendRes, weakRes] = await Promise.all([
      getScoreTrend(studentId),
      getWeakPoints(studentId)
    ])
    
    console.log('成绩趋势响应:', trendRes)
    console.log('薄弱知识点响应:', weakRes)
    
    // 等待DOM更新后再初始化图表
    await nextTick()
    
    // 处理成绩趋势数据
    if (trendRes.code === 200 || trendRes.code === '200') {
      const trendData = trendRes.data || []
      console.log('趋势数据:', trendData)
      
      if (trendData.length > 0) {
        initTrendChart(trendData)
      } else {
        console.warn('趋势数据为空')
        // 显示空状态或默认数据
        trendChartRef.value.innerHTML = '<div style="text-align:center;padding-top:100px;color:#999;">暂无成绩数据</div>'
      }
    } else {
      console.error('获取趋势数据失败:', trendRes.msg)
    }
    
    // 处理薄弱知识点数据
    if (weakRes.code === 200 || weakRes.code === '200') {
      weakPoints.value = weakRes.data || []
      console.log('薄弱知识点数据:', weakPoints.value)
      
      if (weakPoints.value.length > 0) {
        initWeakChart(weakPoints.value)
      } else {
        console.warn('薄弱知识点数据为空')
        weakChartRef.value.innerHTML = '<div style="text-align:center;padding-top:100px;color:#999;">暂无薄弱知识点</div>'
      }
    } else {
      console.error('获取薄弱知识点失败:', weakRes.msg)
    }
    
  } catch (err) {
    console.error('加载数据失败:', err)
    ElMessage.error('加载学情数据失败')
  } finally {
    loading.value = false
  }
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  weakChart?.dispose()
})
</script>

<style scoped>
.dashboard {
  min-height: 100%;
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-text {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.weak-points-container {
  padding: 10px 0;
}

.weak-point-item {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
  transition: all 0.3s;
}

.weak-point-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.point-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.point-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.point-action {
  margin-top: 10px;
  text-align: right;
}

/* 确保图表容器有明确尺寸 */
:deep(.el-card__body) {
  padding: 10px;
}
</style>