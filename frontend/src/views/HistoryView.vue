<template>
  <div class="history-container">
    <h2>问答历史</h2>
    <el-table :data="histories" stripe v-loading="loading">
      <el-table-column prop="title" label="对话标题" min-width="200" />
      <el-table-column prop="messageCount" label="消息数" width="100" />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="primary" link @click="viewMessages(row.id)">查看</el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button type="danger" link>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showMessages" :title="currentTitle" width="700px">
      <div v-for="msg in currentMessages" :key="msg.id" class="msg-item">
        <el-tag :type="msg.role === 'USER' ? 'primary' : 'success'" size="small">{{ msg.role === 'USER' ? '用户' : 'AI' }}</el-tag>
        <div class="msg-content">{{ msg.content }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getChatHistories, getChatMessages, deleteChatHistory } from '@/api/chat'
import { ElMessage } from 'element-plus'

const histories = ref<any[]>([])
const loading = ref(false)
const showMessages = ref(false)
const currentTitle = ref('')
const currentMessages = ref<any[]>([])

onMounted(() => loadHistories())

async function loadHistories() {
  loading.value = true
  try {
    const res: any = await getChatHistories()
    histories.value = res.data?.list || []
  } finally {
    loading.value = false
  }
}

async function viewMessages(id: number) {
  const res: any = await getChatMessages(id)
  currentMessages.value = res.data || []
  const history = histories.value.find((h: any) => h.id === id)
  currentTitle.value = history?.title || '对话详情'
  showMessages.value = true
}

async function handleDelete(id: number) {
  await deleteChatHistory(id)
  ElMessage.success('删除成功')
  loadHistories()
}
</script>

<style scoped>
.history-container {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.history-container h2 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 20px;
}
.msg-item {
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}
.msg-content {
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
}
</style>
