<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <div id="trendChart" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div id="weakChart" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>薄弱知识点</span>
          </template>
          <el-tag v-for="item in weakPoints" :key="item" type="danger" style="margin-right: 10px;">{{ item }}</el-tag>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { getScoreTrend, getWeakPoints } from '@/api/academic'
import { ElMessage } from 'element-plus'

const weakPoints = ref([])

onMounted(async () => {
  try {
    // 假设当前用户ID为1（实际应从用户信息获取）
    const trendRes = await getScoreTrend(1)
    const weakRes = await getWeakPoints(1)
    if (trendRes.code === 200) {
      initTrendChart(trendRes.data)
    }
    if (weakRes.code === 200) {
      weakPoints.value = weakRes.data
      initWeakChart(weakRes.data)
    }
  } catch (err) {
    ElMessage.error('加载学情数据失败')
  }
})

const initTrendChart = (data) => {
  const chart = echarts.init(document.getElementById('trendChart'))
  chart.setOption({
    title: { text: '成绩趋势' },
    tooltip: {},
    xAxis: { type: 'category', data: data.map(item => item.date) },
    yAxis: { type: 'value' },
    series: [{ data: data.map(item => item.score), type: 'line', smooth: true }]
  })
}

const initWeakChart = (data) => {
  const chart = echarts.init(document.getElementById('weakChart'))
  chart.setOption({
    title: { text: '知识点掌握度' },
    tooltip: {},
    xAxis: { type: 'category', data: data.map(item => item.knowledge) },
    yAxis: { type: 'value', max: 100 },
    series: [{ data: data.map(item => item.mastery), type: 'bar' }]
  })
}
</script>