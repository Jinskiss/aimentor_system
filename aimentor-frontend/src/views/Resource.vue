<template>
  <div class="resource">
    <el-row :gutter="20">
      <el-col v-for="item in resources" :key="item.id" :span="8">
        <el-card class="resource-card">
          <template #header>
            <span>{{ item.title }}</span>
          </template>
          <div>{{ item.description }}</div>
          <div style="margin-top: 10px;">
            <el-tag size="small">{{ item.type }}</el-tag>
          </div>
          <template #footer>
            <el-button type="text" @click="openResource(item.url)">查看详情</el-button>
          </template>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getRecommendResources } from '@/api/resource'
import { ElMessage } from 'element-plus'

const resources = ref([])

onMounted(async () => {
  try {
    const res = await getRecommendResources()
    if (res.code === 200) {
      resources.value = res.data
    }
  } catch (err) {
    ElMessage.error('加载资源失败')
  }
})

const openResource = (url) => {
  window.open(url, '_blank')
}
</script>

<style scoped>
.resource-card {
  margin-bottom: 20px;
}
</style>