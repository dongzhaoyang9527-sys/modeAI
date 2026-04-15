<template>
  <div class="admin-container">
    <h2>系统监控</h2>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>系统信息</template>
          <el-descriptions :column="1" border>
            <el-descriptions-item v-for="(value, key) in systemInfo" :key="key" :label="String(key)">
              {{ value }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>Token 消耗概览</template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="统计周期">{{ tokenSummary.periodDays }} 天</el-descriptions-item>
            <el-descriptions-item label="总消耗">{{ tokenSummary.totalTokens?.toLocaleString() }}</el-descriptions-item>
            <el-descriptions-item label="日均消耗">{{ tokenSummary.avgDailyTokens?.toLocaleString() }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const systemInfo = ref<Record<string, string>>({})
const tokenSummary = ref<any>({})

onMounted(async () => {
  try {
    const [sysRes, tokenRes]: any[] = await Promise.all([
      request.get('/admin/monitor/system-info'),
      request.get('/admin/monitor/token-usage/summary')
    ])
    systemInfo.value = sysRes.data || {}
    tokenSummary.value = tokenRes.data || {}
  } catch (e) {}
})
</script>

<style scoped>
.admin-container {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.admin-container h2 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 20px;
}
</style>
