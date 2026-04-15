<template>
  <div class="admin-container">
    <h2>角色管理</h2>
    <el-table :data="roles" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="roleName" label="角色名称" width="150" />
      <el-table-column prop="roleCode" label="角色编码" width="150" />
      <el-table-column prop="description" label="描述" min-width="200" />
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const roles = ref<any[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res: any = await request.get('/admin/roles')
    roles.value = res.data || []
  } finally {
    loading.value = false
  }
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
