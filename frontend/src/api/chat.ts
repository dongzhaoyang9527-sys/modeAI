import request from './request'
import { useUserStore } from '@/stores/user'

export interface ChatRequest {
  message: string
  conversationId?: string
}

export function streamChat(data: ChatRequest, onMessage: (text: string) => void, onDone: () => void) {
  const userStore = useUserStore()
  const token = userStore.token

  fetch('/api/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
      'Accept': 'text/event-stream'
    },
    body: JSON.stringify(data)
  }).then(response => {
    const reader = response.body?.getReader()
    const decoder = new TextDecoder()

    function read() {
      reader?.read().then(({ done, value }) => {
        if (done) {
          onDone()
          return
        }
        const text = decoder.decode(value, { stream: true })
        onMessage(text)
        read()
      })
    }
    read()
  }).catch(err => {
    console.error('Stream error:', err)
    onDone()
  })
}

export function getChatHistories(page: number = 1, size: number = 20) {
  return request.get('/chat/histories', { params: { page, size } })
}

export function getChatMessages(historyId: number) {
  return request.get(`/chat/histories/${historyId}/messages`)
}

export function deleteChatHistory(historyId: number) {
  return request.delete(`/chat/histories/${historyId}`)
}
