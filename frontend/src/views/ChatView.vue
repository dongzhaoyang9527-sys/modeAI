<template>
  <div class="chat-container">
    <div class="chat-messages" ref="messagesRef">
      <div v-if="messages.length === 0" class="chat-empty">
        <el-icon :size="64" color="#c0c4cc"><ChatDotRound /></el-icon>
        <h2>您好，我是 modeAI 智能助手</h2>
        <p>我可以帮您查询企业知识库中的信息，请随时提问。</p>
      </div>
      <div v-for="(msg, index) in messages" :key="index" :class="['chat-message', msg.role.toLowerCase()]">
        <div class="message-avatar">
          <el-avatar v-if="msg.role === 'USER'" :size="36" icon="UserFilled" />
          <el-avatar v-else :size="36" style="background: #409eff">AI</el-avatar>
        </div>
        <div class="message-content">
          <div class="message-bubble" v-html="renderMarkdown(msg.content)"></div>
        </div>
      </div>
      <div v-if="isStreaming" class="chat-message assistant">
        <div class="message-avatar">
          <el-avatar :size="36" style="background: #409eff">AI</el-avatar>
        </div>
        <div class="message-content">
          <div class="message-bubble">
            <span v-html="renderMarkdown(streamingContent)"></span>
            <span class="cursor-blink">|</span>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input-area">
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="2"
        placeholder="请输入您的问题..."
        resize="none"
        @keydown.enter.exact.prevent="sendMessage"
        :disabled="isStreaming"
      />
      <el-button type="primary" :icon="Promotion" :loading="isStreaming" @click="sendMessage" :disabled="!inputMessage.trim()">
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { Promotion } from '@element-plus/icons-vue'
import { streamChat } from '@/api/chat'
import { marked } from 'marked'

interface Message {
  role: 'USER' | 'ASSISTANT'
  content: string
}

const messages = ref<Message[]>([])
const inputMessage = ref('')
const isStreaming = ref(false)
const streamingContent = ref('')
const messagesRef = ref<HTMLElement>()

function renderMarkdown(text: string): string {
  return marked(text || '', { breaks: true })
}

async function sendMessage() {
  const msg = inputMessage.value.trim()
  if (!msg || isStreaming.value) return

  messages.value.push({ role: 'USER', content: msg })
  inputMessage.value = ''
  isStreaming.value = true
  streamingContent.value = ''

  await nextTick()
  scrollToBottom()

  streamChat(
    { message: msg, conversationId: '' },
    (text: string) => {
      streamingContent.value += text
      scrollToBottom()
    },
    () => {
      if (streamingContent.value) {
        messages.value.push({ role: 'ASSISTANT', content: streamingContent.value })
      }
      streamingContent.value = ''
      isStreaming.value = false
      scrollToBottom()
    }
  )
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}
.chat-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}
.chat-empty h2 {
  margin-top: 16px;
  font-size: 20px;
  color: #606266;
}
.chat-empty p {
  margin-top: 8px;
  font-size: 14px;
}
.chat-message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
.chat-message.user {
  flex-direction: row-reverse;
}
.message-content {
  max-width: 70%;
}
.message-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
}
.chat-message.user .message-bubble {
  background: #409eff;
  color: #fff;
  border-top-right-radius: 4px;
}
.chat-message.assistant .message-bubble {
  background: #f4f4f5;
  color: #303133;
  border-top-left-radius: 4px;
}
.cursor-blink {
  animation: blink 1s infinite;
  color: #409eff;
}
@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}
.chat-input-area {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #ebeef5;
  background: #fafafa;
  align-items: flex-end;
}
.chat-input-area .el-textarea {
  flex: 1;
}
</style>
