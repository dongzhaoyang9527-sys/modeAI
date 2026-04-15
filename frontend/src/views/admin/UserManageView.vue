<template>
  <div class="admin-container">
    <h2>用户管理</h2>
    <el-table :data="users" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="enabled" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">{{ row.enabled ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="primary" link @click="toggleUser(row)">{{ row.enabled ? '禁用' : '启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const users = ref<any[]>([])
const loading = ref(false)

onMounted(() => loadUsers())

async function loadUsers() {
  loading.value = true
  try {
    const res: any = await request.get('/admin/users')
    users.value = res.data?.list || []
  } finally {
    loading.value = false
  }
}

async function toggleUser(user: any) {
  await request.put(`/admin/users/${user.id}`, { enabled: !user.enabled })
  loadUsers()
}
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
