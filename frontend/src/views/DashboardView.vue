<template>
  <div class="dashboard-container">
    <h2>仪表盘</h2>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409eff20; color: #409eff"><el-icon :size="28"><Document /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalDocs }}</div>
              <div class="stat-label">文档总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #67c23a20; color: #67c23a"><el-icon :size="28"><ChatDotRound /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalChats }}</div>
              <div class="stat-label">问答次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #e6a23c20; color: #e6a23c"><el-icon :size="28"><Coin /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalTokens }}</div>
              <div class="stat-label">Token 消耗</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #f56c6c20; color: #f56c6c"><el-icon :size="28"><User /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalUsers }}</div>
              <div class="stat-label">用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const stats = ref({ totalDocs: 0, totalChats: 0, totalTokens: 0, totalUsers: 0 })

onMounted(async () => {
  try {
    const [docRes, monitorRes]: any[] = await Promise.all([
      request.get('/admin/documents/stats'),
      request.get('/admin/monitor/token-usage/summary')
    ])
    stats.value.totalDocs = docRes.data?.completed || 0
    stats.value.totalTokens = monitorRes.data?.totalTokens || 0
  } catch (e) {}
})
</script>

<style scoped>
.dashboard-container {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.dashboard-container h2 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 20px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
</style>
