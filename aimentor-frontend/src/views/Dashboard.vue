<template>
  <div class="dashboard">
    <!-- 页面整体采用 el-row/el-col 栅格布局，gutter 控制卡片之间的间距 -->
    <!-- ============================================================
         第一行：两个并排的图表卡片
         - 左侧（el-col :span="12"）：成绩趋势折线图
         - 右侧（el-col :span="12"）：知识点掌握度柱状图
         ============================================================ -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <!-- ref="trendChartRef" 用于在 JS 中获取 DOM 并初始化 ECharts 实例 -->
          <div ref="trendChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <!-- ref="weakChartRef" 同上，用于渲染知识点掌握度柱状图 -->
          <div ref="weakChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ============================================================
         第二行：薄弱知识点详情卡片（占满整行 24 栏）
         仅展示"掌握度低于 60%"的知识点，供学生针对性复习参考
         ============================================================ -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>薄弱知识点</span>
              <!-- 只有当有薄弱知识点时才显示提示标签 -->
              <el-tag v-if="weakPoints.length > 0" type="info" size="small">
                掌握度低于60%
              </el-tag>
            </div>
          </template>

          <!-- 加载中状态：当 loading=true 时显示提示文字 -->
          <div v-if="loading" class="loading-text">加载中...</div>

          <!-- 薄弱知识点列表：当 weakPoints.length > 0 时才渲染卡片网格 -->
          <div v-else-if="weakPoints.length > 0" class="weak-points-container">
            <el-row :gutter="15">
              <!--
                响应式布局：移动端 xs=24（全宽）| 小屏 sm=12（一行两块）
                | 中屏 md=8（一行三块）| 大屏 lg=6（一行四块）
              -->
              <el-col
                v-for="item in weakPoints"
                :key="item.knowledge"
                :xs="24"
                :sm="12"
                :md="8"
                :lg="6"
              >
                <!-- 薄弱点卡片：鼠标悬停时上浮并加阴影，提升交互体验 -->
                <div class="weak-point-item">
                  <div class="point-info">
                    <!-- 知识点名称 + 掌握度标签（颜色由 getMasteryType 决定） -->
                    <span class="point-name">{{ item.knowledge }}</span>
                    <el-tag :type="getMasteryType(item.mastery)" size="small">
                      {{ item.mastery }}分
                    </el-tag>
                  </div>

                  <!-- 进度条直观展示掌握度百分比，颜色由 getMasteryColor 决定 -->
                  <el-progress
                    :percentage="item.mastery"
                    :color="getMasteryColor(item.mastery)"
                    :stroke-width="8"
                    :show-text="false"
                  />

                  <!-- "去练习"按钮，引导学生针对薄弱项做强化训练（后续可链接到练习模块） -->
                  <div class="point-action">
                    <el-button type="primary" link size="small">
                      去练习 →
                    </el-button>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>

          <!-- 空状态：当既不在加载中、也没有薄弱知识点时显示此提示 -->
          <el-empty v-else description="暂无薄弱知识点，继续保持！" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
/**
 * Dashboard.vue — 学情信息（仪表盘）页面
 *
 * 功能说明：
 *   1. 页面加载时，调用后端「当前登录用户」的学情数据接口：
 *        GET /api/academic/trend      → 成绩趋势折线图
 *        GET /api/academic/masteries  → 知识点掌握度柱状图（全部数据）
 *        GET /api/academic/weakpoints → 薄弱知识点卡片（mastery < 60%）
 *   2. 学生 ID 从 UserHolder（ThreadLocal）获取，不再拼到 URL 路径中，
 *      避免雪花 ID（Long）经 JSON 解析为 JS number 后精度丢失导致查不到数据。
 *   3. ECharts 图表在窗口大小变化时自动 resize，保证响应式布局正确。
 *
 * 依赖：
 *   - ECharts（动态 import，按需加载，减少首屏体积）
 *   - Pinia store：useUserStore（获取登录状态）
 *   - api/academic.js：封装所有学情相关接口调用
 */

import { onMounted, onUnmounted, ref, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getScoreTrend, getWeakPoints, getKnowledgeMasteries } from '@/api/academic'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// ================================================================
// 图表 DOM 引用（通过 ref="xxxRef" 绑定到模板中的 div）
// ================================================================
/** 成绩趋势折线图 DOM 容器引用 */
const trendChartRef = ref(null)
/** 知识点掌握度柱状图 DOM 容器引用 */
const weakChartRef = ref(null)

// ================================================================
// 页面数据
// ================================================================
/** 薄弱知识点列表（mastery < 60%），用于渲染下方卡片网格 */
const weakPoints = ref([])
/** 全局加载状态，true 时在薄弱知识点区显示骨架屏 */
const loading = ref(false)

// ================================================================
// ECharts 图表实例（全局变量，页面卸载时需 dispose 释放内存）
// ================================================================
/** 成绩趋势折线图 ECharts 实例，dispose 时需判空 */
let trendChart = null
/** 知识点掌握度柱状图 ECharts 实例，dispose 时需判空 */
let weakChart = null

/** Pinia 用户状态仓库，用于检查登录状态和获取用户信息 */
const userStore = useUserStore()

// ================================================================
// 辅助方法 — 掌握度样式映射
// ================================================================

/**
 * 根据掌握度返回 Element Plus el-tag 类型
 * @param {number} mastery 掌握度（0-100）
 * @returns {string} 'danger'|'warning'|'success'
 *
 * 阈值说明：
 *   - < 30 → 危险/薄弱（红色）
 *   - < 60 → 一般/待提升（橙色）
 *   - >= 60 → 良好/已掌握（绿色）
 */
const getMasteryType = (mastery) => {
  if (mastery < 30) return 'danger'
  if (mastery < 60) return 'warning'
  return 'success'
}

/**
 * 根据掌握度返回 ECharts 进度条颜色
 * @param {number} mastery 掌握度（0-100）
 * @returns {string} 十六进制颜色值
 */
const getMasteryColor = (mastery) => {
  if (mastery < 30) return '#F56C6C'
  if (mastery < 60) return '#E6A23C'
  return '#67C23A'
}

// ================================================================
// ECharts 图表初始化
// ================================================================

/**
 * 初始化成绩趋势折线图（ECharts line chart）
 *
 * 数据结构：data 为数组，每项包含 dateMonth（如 "2024-03"）和 avgScore（平均分）
 *
 * 图表特性：
 *   - 平滑曲线（smooth: true）
 *   - 数据点用空心圆标记（symbol: 'circle'）
 *   - 面积渐变填充，增强视觉效果
 *   - tooltip 鼠标悬停显示具体月份和分数
 *
 * @param {Array} data 成绩趋势数据，元素形如 { dateMonth: string, avgScore: number }
 */
const initTrendChart = (data) => {
  console.log('[Dashboard] 初始化成绩趋势折线图，接收数据条目数:', data.length, '，数据详情:', data)

  // DOM 元素不存在时跳过初始化（防止 SSR 或路由切换时图表未挂载）
  if (!trendChartRef.value) {
    console.error('[Dashboard] 成绩趋势图 DOM 容器不存在，跳过初始化')
    return
  }

  // 若已有 ECharts 实例，先销毁以避免重复渲染
  if (trendChart) {
    console.log('[Dashboard] 销毁旧的成绩趋势图实例，准备重新渲染')
    trendChart.dispose()
    trendChart = null
  }

  // 新建 ECharts 实例并绑定 DOM
  trendChart = echarts.init(trendChartRef.value)
  console.log('[Dashboard] 成绩趋势图 ECharts 实例已创建，DOM:', trendChartRef.value)

  // ECharts 配置项
  const option = {
    // 图表标题：居中显示
    title: {
      text: '成绩趋势',
      left: 'center',
      top: 10
    },
    // 鼠标悬停提示：显示月份和分数
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>分数: {c}分'
    },
    // 绘图网格：控制图表在容器中的边距
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true   // 自动把轴标签纳入网格，防止标签溢出
    },
    // X 轴：类目轴，数据显示月份
    xAxis: {
      type: 'category',
      // 后端返回 dateMonth 字段（如 "2024-03"），用于展示横轴
      data: data.map(item => item.dateMonth),
      boundaryGap: false,  // 折线图 boundaryGap=false 使线条从原点开始
      axisLine: { lineStyle: { color: '#999' } }
    },
    // Y 轴：数值轴，范围固定 0-100（百分制）
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLine: { lineStyle: { color: '#999' } },
      splitLine: { lineStyle: { color: '#eee' } }  // 横向网格虚线
    },
    // 数据系列：平滑折线 + 面积渐变
    series: [{
      // avgScore 为后端 SQL 聚合得到的月均分
      data: data.map(item => item.avgScore),
      type: 'line',
      smooth: true,       // 平滑曲线
      symbol: 'circle',
      symbolSize: 8,      // 数据点大小
      lineStyle: { color: '#409EFF', width: 3 },
      itemStyle: { color: '#409EFF', borderWidth: 2, borderColor: '#fff' },
      // 面积渐变：从上到下从蓝色半透明过渡到几乎透明
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
        ])
      }
    }]
  }

  // 渲染图表
  trendChart.setOption(option)
  console.log('[Dashboard] 成绩趋势折线图已渲染，X轴数据:', data.map(i => i.dateMonth), '，Y轴数据:', data.map(i => i.avgScore))
}

/**
 * 初始化知识点掌握度柱状图（ECharts bar chart）
 *
 * 数据来源：/api/academic/masteries（全部知识点掌握度，非仅薄弱点），
 * 因为 /api/academic/weakpoints 只返回 mastery < 60% 的数据，
 * 若只用薄弱点画柱状图，大多数时候图表会为空。
 *
 * 柱条颜色规则：
 *   - 掌握度 < 60% → 红色（需重点关注）
 *   - 掌握度 >= 60% → 绿色（已达标）
 *
 * @param {Array} data 知识点掌握度数据，元素形如 { knowledge: string, mastery: number, subject?: string }
 */
const initWeakChart = (data) => {
  console.log('[Dashboard] 初始化知识点掌握度柱状图，接收数据条目数:', data.length, '，数据详情:', data)

  if (!weakChartRef.value) {
    console.error('[Dashboard] 掌握度柱状图 DOM 容器不存在，跳过初始化')
    return
  }

  if (weakChart) {
    console.log('[Dashboard] 销毁旧的掌握度柱状图实例，准备重新渲染')
    weakChart.dispose()
    weakChart = null
  }

  weakChart = echarts.init(weakChartRef.value)

  // 根据每条数据的掌握度动态决定柱条颜色
  const barData = data.map(item => ({
    value: item.mastery,
    itemStyle: {
      // 掌握度低于 60% 显示红色，提醒学生关注；其余显示绿色
      color: item.mastery < 60 ? '#F56C6C' : '#67C23A'
    }
  }))

  const option = {
    title: {
      text: '知识点掌握度',
      left: 'center',
      top: 10
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },   // 鼠标悬停时显示阴影指示线
      formatter: '{b}<br/>掌握度: {c}%'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    // X 轴：知识点名称作为横轴标签
    xAxis: {
      type: 'category',
      data: data.map(item => item.knowledge),
      axisLine: { lineStyle: { color: '#999' } },
      // 知识点过多（>5 个）时标签旋转 30 度防止重叠
      axisLabel: { interval: 0, rotate: data.length > 5 ? 30 : 0 }
    },
    // Y 轴：0-100，显示百分比
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: { formatter: '{value}%' },
      splitLine: { lineStyle: { color: '#eee' } }
    },
    // 数据系列：柱状图，柱宽占类目宽度的 50%
    series: [{
      data: barData,
      type: 'bar',
      barWidth: '50%',
      // 柱条顶部显示百分比数值
      label: {
        show: true,
        position: 'top',
        formatter: '{c}%'
      }
    }]
  }

  weakChart.setOption(option)
  console.log('[Dashboard] 知识点掌握度柱状图已渲染，知识点列表:', data.map(i => i.knowledge))
}

/**
 * 窗口大小变化时触发图表 resize，保证图表始终撑满容器
 * 绑定到 window.resize 事件，页面卸载时移除防止内存泄漏
 */
const handleResize = () => {
  console.log('[Dashboard] 窗口大小变化，触发图表 resize')
  trendChart?.resize()
  weakChart?.resize()
}

// ================================================================
// 生命周期钩子
// ================================================================

/**
 * 组件挂载时执行：
 *   1. 检查登录状态，未登录则提示并阻止后续请求
 *   2. 并行调用三个学情数据接口（Promise.all），提升加载速度
 *   3. 根据接口返回渲染图表和数据列表
 */
onMounted(async () => {
  console.log('[Dashboard] 组件已挂载，开始加载学情数据')
  loading.value = true

  try {
    // ----------------------------------------------------------
    // 登录状态检查：
    // 若 localStorage 有 token 但 userInfo 尚未拉取，先 fetchUserInfo()
    // 以确保后续接口能正确携带用户信息
    // ----------------------------------------------------------
    if (userStore.isLoggedIn && !userStore.userInfo) {
      console.log('[Dashboard] 已登录但 userInfo 为空，调用 fetchUserInfo() 获取用户信息')
      await userStore.fetchUserInfo()
    }
    if (!userStore.isLoggedIn) {
      console.warn('[Dashboard] 用户未登录，中断学情数据加载')
      ElMessage.error('请先登录')
      return
    }
    console.log('[Dashboard] 当前用户:', userStore.userInfo, '，开始请求学情数据')

    // ----------------------------------------------------------
    // 并行请求三个接口，避免串行等待：
    //   getScoreTrend()        → GET /api/academic/trend      （成绩趋势）
    //   getWeakPoints()        → GET /api/academic/weakpoints（薄弱点）
    //   getKnowledgeMasteries() → GET /api/academic/masteries（全部掌握度）
    // ----------------------------------------------------------
    console.log('[Dashboard] 并行请求学情数据接口...')
    const [trendRes, weakRes, allMasteryRes] = await Promise.all([
      getScoreTrend(),
      getWeakPoints(),
      getKnowledgeMasteries()
    ])

    console.log('[Dashboard] 成绩趋势接口响应:', trendRes)
    console.log('[Dashboard] 薄弱知识点接口响应:', weakRes)
    console.log('[Dashboard] 全部掌握度接口响应:', allMasteryRes)

    // ----------------------------------------------------------
    // 等待 Vue DOM 更新完成后再初始化 ECharts
    // 因为 ECharts 需要 DOM 元素已渲染才能正确计算尺寸
    // ----------------------------------------------------------
    await nextTick()
    console.log('[Dashboard] DOM 已更新，开始渲染图表')

    // ----------------------------------------------------------
    // 处理成绩趋势数据 → 渲染折线图
    // ----------------------------------------------------------
    if (trendRes.code === 200 || trendRes.code === '200') {
      const trendData = trendRes.data || []
      console.log('[Dashboard] 趋势数据条目数:', trendData.length)

      if (trendData.length > 0) {
        console.log('[Dashboard] 趋势数据非空，渲染折线图，数据:', trendData)
        initTrendChart(trendData)
      } else {
        // 没有成绩数据时，在图表区域显示空状态提示，并给出引导文案
        console.warn('[Dashboard] 暂无成绩趋势数据，显示空状态提示')
        trendChartRef.value.innerHTML =
          '<div style="text-align:center;padding-top:100px;color:#999;">暂无成绩数据，请先在「学情记录」中添加成绩记录</div>'
      }
    } else {
      console.error('[Dashboard] 获取成绩趋势失败，错误信息:', trendRes.msg)
    }

    // ----------------------------------------------------------
    // 柱状图：展示全部知识点掌握度
    // 重要：这里用 /masteries（全部数据），不用 /weakpoints（仅 <60% 的项）
    // 否则大多数情况下柱状图会为空，误导用户以为系统无数据
    // ----------------------------------------------------------
    if (allMasteryRes.code === 200 || allMasteryRes.code === '200') {
      const masteryList = allMasteryRes.data || []
      console.log('[Dashboard] 全部掌握度数据条目数:', masteryList.length)

      if (masteryList.length > 0) {
        console.log('[Dashboard] 掌握度数据非空，渲染柱状图，数据:', masteryList)
        initWeakChart(masteryList)
      } else {
        console.warn('[Dashboard] 暂无知识点掌握度数据，显示空状态提示')
        weakChartRef.value.innerHTML =
          '<div style="text-align:center;padding-top:100px;color:#999;">暂无知识点掌握度数据，请先在「学情记录」中添加</div>'
      }
    } else {
      console.error('[Dashboard] 获取全部掌握度列表失败，错误信息:', allMasteryRes.msg)
    }

    // ----------------------------------------------------------
    // 下方薄弱知识点卡片：仅展示 mastery < 60% 的项
    // 此数据来自 /api/academic/weakpoints（后端 SQL 已过滤）
    // ----------------------------------------------------------
    if (weakRes.code === 200 || weakRes.code === '200') {
      weakPoints.value = weakRes.data || []
      console.log('[Dashboard] 薄弱知识点条目数:', weakPoints.value.length, '，数据:', weakPoints.value)
    } else {
      console.error('[Dashboard] 获取薄弱知识点失败，错误信息:', weakRes.msg)
    }

  } catch (err) {
    // 捕获整个加载流程中的任何异常（如网络错误、接口 500 等）
    console.error('[Dashboard] 加载学情数据时捕获到异常:', err)
    ElMessage.error('加载学情数据失败，请检查网络或重新登录')
  } finally {
    // 不管成功失败都必须关闭 loading 状态，防止界面卡在加载中
    loading.value = false
    console.log('[Dashboard] 学情数据加载流程结束，loading 已置为 false')
  }

  // ----------------------------------------------------------
  // 监听浏览器窗口大小变化，自动重绘图表
  // ----------------------------------------------------------
  window.addEventListener('resize', handleResize)
  console.log('[Dashboard] 已注册 window.resize 事件监听')
})

/**
 * 组件卸载时执行清理：
 *   1. 移除 window.resize 事件监听，防止页面切换后仍响应 resize
 *   2. 销毁 ECharts 实例，释放 Canvas 内存
 */
onUnmounted(() => {
  console.log('[Dashboard] 组件即将卸载，执行清理操作')

  window.removeEventListener('resize', handleResize)
  console.log('[Dashboard] 已移除 window.resize 事件监听')

  // 可选链操作符 ?.：若实例为 null/undefined 不会报错
  trendChart?.dispose()
  weakChart?.dispose()
  console.log('[Dashboard] 已销毁 ECharts 实例，释放内存')
})
</script>

<style scoped>
/* 页面容器：最小高度 100%，padding 防止内容贴边 */
.dashboard {
  min-height: 100%;
  padding: 10px;
}

/* 卡片头部：标题和标签左对齐，按钮右对齐 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 加载中文字样式 */
.loading-text {
  text-align: center;
  padding: 40px;
  color: #909399;
}

/* 薄弱知识点网格容器，内边距使卡片与标题保持一定间距 */
.weak-points-container {
  padding: 10px 0;
}

/* 薄弱点单项卡片样式 */
.weak-point-item {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
  transition: all 0.3s; /* 过渡动画：hover 时平滑变换 */
}

/* 鼠标悬停效果：上浮 + 阴影加深，提升交互反馈感 */
.weak-point-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

/* 知识点名称 + 标签所在行：左右分布 */
.point-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

/* 知识点名称：粗体深灰色 */
.point-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

/* "去练习"按钮右对齐，视觉上更整洁 */
.point-action {
  margin-top: 10px;
  text-align: right;
}

/* 深度选择器：强制让 el-card 内容区左右间距收紧，使图表有更多空间 */
:deep(.el-card__body) {
  padding: 10px;
}
</style>