import request from './request'

export function uploadDocument(file: File, departmentId?: number, accessLevel?: number) {
  const formData = new FormData()
  formData.append('file', file)
  if (departmentId) formData.append('departmentId', String(departmentId))
  if (accessLevel) formData.append('accessLevel', String(accessLevel))
  return request.post('/documents/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function listDocuments(params: { keyword?: string; status?: string; page?: number; size?: number }) {
  return request.get('/documents', { params })
}

export function deleteDocument(id: number) {
  return request.delete(`/documents/${id}`)
}
