<template>
  <div class="document-container">
    <div class="doc-header">
      <h2>文档管理</h2>
      <el-upload
        :action="'/api/documents/upload'"
        :headers="uploadHeaders"
        :show-file-list="false"
        :before-upload="beforeUpload"
        :on-success="onUploadSuccess"
        :on-error="onUploadError"
        accept=".pdf,.docx,.doc,.md,.txt"
      >
        <el-button type="primary" icon="Upload">上传文档</el-button>
      </el-upload>
    </div>

    <div class="doc-toolbar">
      <el-input v-model="keyword" placeholder="搜索文档..." prefix-icon="Search" clearable style="width: 300px" @clear="loadDocuments" @keyup.enter="loadDocuments" />
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 150px" @change="loadDocuments">
        <el-option label="待处理" value="PENDING" />
        <el-option label="处理中" value="PROCESSING" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="失败" value="FAILED" />
      </el-select>
    </div>

    <el-table :data="documents" stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="title" label="文档名称" min-width="200" />
      <el-table-column prop="fileType" label="类型" width="80" />
      <el-table-column prop="fileSize" label="大小" width="120">
        <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="chunkCount" label="分块数" width="80" />
      <el-table-column prop="createdAt" label="上传时间" width="180" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-popconfirm title="确定删除该文档？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button type="danger" link icon="Delete">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-area">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadDocuments"
        @current-change="loadDocuments"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { listDocuments, deleteDocument } from '@/api/document'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const documents = ref<any[]>([])
const loading = ref(false)
const keyword = ref('')
const statusFilter = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

onMounted(() => loadDocuments())

async function loadDocuments() {
  loading.value = true
  try {
    const res: any = await listDocuments({ keyword: keyword.value, status: statusFilter.value, page: page.value, size: size.value })
    documents.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function beforeUpload(file: File) {
  const allowed = ['pdf', 'docx', 'doc', 'md', 'txt']
  const ext = file.name.split('.').pop()?.toLowerCase()
  if (!allowed.includes(ext || '')) {
    ElMessage.error('不支持的文件类型')
    return false
  }
  if (file.size > 100 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过100MB')
    return false
  }
  return true
}

function onUploadSuccess(response: any) {
  if (response.code === 200) {
    ElMessage.success('文档上传成功')
    loadDocuments()
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

function onUploadError() {
  ElMessage.error('上传失败')
}

async function handleDelete(id: number) {
  await deleteDocument(id)
  ElMessage.success('删除成功')
  loadDocuments()
}

function formatFileSize(bytes: number): string {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  while (bytes >= 1024 && i < units.length - 1) { bytes /= 1024; i++ }
  return bytes.toFixed(1) + ' ' + units[i]
}

function statusTagType(status: string) {
  const map: Record<string, string> = { PENDING: 'info', PROCESSING: 'warning', COMPLETED: 'success', FAILED: 'danger' }
  return map[status] || 'info'
}

function statusLabel(status: string) {
  const map: Record<string, string> = { PENDING: '待处理', PROCESSING: '处理中', COMPLETED: '已完成', FAILED: '失败' }
  return map[status] || status
}
</script>

<style scoped>
.document-container {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.doc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.doc-header h2 {
  font-size: 18px;
  color: #303133;
}
.doc-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.pagination-area {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
