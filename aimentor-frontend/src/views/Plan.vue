<template>
  <div class="plan">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between;">
          <span>我的学习计划</span>
          <el-button type="primary" @click="handleGenerate">生成新计划</el-button>
        </div>
      </template>
      <el-table :data="plans" stripe>
        <el-table-column prop="content" label="计划内容" />
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '已完成' ? 'success' : 'warning'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button v-if="row.status !== '已完成'" size="small" @click="handleComplete(row.id)">标记完成</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyPlans, generatePlan, updatePlanStatus } from '@/api/plan'
import { ElMessage, ElMessageBox } from 'element-plus'

const plans = ref([])

onMounted(async () => {
  await loadPlans()
})

const loadPlans = async () => {
  try {
    const res = await getMyPlans()
    if (res.code === 200) {
      plans.value = res.data
    }
  } catch (err) {
    ElMessage.error('加载计划失败')
  }
}

const handleGenerate = async () => {
  try {
    const res = await generatePlan({})
    if (res.code === 200) {
      ElMessage.success('计划生成成功')
      await loadPlans()
    } else {
      ElMessage.error(res.msg)
    }
  } catch (err) {
    ElMessage.error('请求失败')
  }
}

const handleComplete = async (id) => {
  try {
    const res = await updatePlanStatus(id, '已完成')
    if (res.code === 200) {
      ElMessage.success('已标记完成')
      await loadPlans()
    }
  } catch (err) {
    ElMessage.error('操作失败')
  }
}
</script>