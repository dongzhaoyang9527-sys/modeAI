# modeAI - 企业级 RAG 智能知识库问答系统

## 项目简介

modeAI 是一个基于 Spring Boot 3 + Spring AI 的企业级 RAG（检索增强生成）智能知识库问答系统，支持文档上传解析、向量化存储、智能问答（流式输出）、基于角色的权限控制等功能。

## 技术栈

### 后端
| 组件 | 技术 | 版本 |
|------|------|------|
| 构建工具 | Gradle | 8.14.2 |
| JDK | OpenJDK | 21 |
| 框架 | Spring Boot | 3.4.2 |
| AI 框架 | Spring AI | 1.0.3 |
| AI 适配 | Spring AI Alibaba (DashScope) | 1.1.2.0 |
| 大模型 | 通义千问 qwen-max | - |
| Embedding | 通义千问 text-embedding-v3 (1024维) | - |
| 向量数据库 | Milvus | 2.4.x |
| 关系数据库 | MySQL | 8.0 |
| 缓存 | Redis | 7.x |
| 消息队列 | RabbitMQ | 3.13 |
| 安全框架 | Spring Security + JWT | - |
| API 文档 | Knife4j (OpenAPI 3) | 4.5.0 |
| 熔断限流 | Resilience4j | 2.2.0 |
| 监控 | Micrometer + Prometheus | - |

### 前端
| 组件 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue 3 + TypeScript | 3.5.x |
| 构建工具 | Vite | 6.x |
| UI 组件库 | Element Plus | 2.9.x |
| 状态管理 | Pinia | 2.3.x |
| HTTP 客户端 | Axios | 1.7.x |
| Markdown | marked + highlight.js | - |

## 项目结构

```
modeAI/
├── rag-common/              # 通用模块 (DTO、异常、工具类)
├── rag-api/                 # API 契约模块 (请求/响应 DTO、枚举)
├── rag-core/                # 核心业务模块 (实体、Service、Repository)
├── rag-infrastructure/      # 基础设施模块 (配置、安全、MQ、AI、缓存)
├── rag-app/                 # 启动模块 (Controller、启动类、配置文件)
├── frontend/                # Vue 3 前端
├── docker-compose.yml       # 本地开发环境编排
└── docs/                    # 项目文档
```

## 快速开始

### Windows 用户

请参阅 **[Windows 环境安装指南](docs/WINDOWS_SETUP.md)** 获取详细的图文安装步骤。

也可直接双击 `start.bat` 一键启动所有服务。

### Linux/Mac 用户

#### 环境要求
- JDK 21+
- Node.js 18+
- Docker & Docker Compose

#### 1. 启动基础设施

```bash
docker-compose up -d
```

这将启动：MySQL 8.0、Redis 7、Milvus 2.4、RabbitMQ 3.13

#### 2. 配置环境变量

```bash
cp .env.example .env
# 编辑 .env 文件，填入 AI_DASHSCOPE_API_KEY
```

#### 3. 启动后端

```bash
./gradlew :rag-app:bootRun
```

后端服务运行在 http://localhost:8080

API 文档: http://localhost:8080/doc.html

#### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端服务运行在 http://localhost:3000

#### 5. 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 超级管理员 |
| zhangsan | 123456 | 普通用户 (技术部) |
| lisi | 123456 | 文档管理员 (产品部) |
| wangwu | 123456 | 普通用户 (运营部) |

## 核心功能

### 文档处理
- 支持 PDF、Word (docx)、Markdown、TXT 格式
- 异步处理：上传 → RabbitMQ 消息 → 解析 → 切分 → Embedding → Milvus 存储
- 文本切分支持段落分割 + 重叠窗口

### 智能问答
- 基于 RAG 的检索增强生成
- SSE 流式输出（打字机效果）
- 对话记忆（Redis 存储，最近 20 条）
- 高频问题答案缓存

### 权限控制
- JWT 认证 + Token 自动续期
- URL 级别权限（Spring Security）
- 文档级别权限（ACL + Milvus 元数据过滤）
- 支持公开、部门、机密、绝密四级访问控制

### 工程化
- 接口限流（Resilience4j RateLimiter）
- 熔断降级（LLM/Milvus 调用熔断）
- 可观测性（Prometheus 指标 + 调用链路追踪）
- Token 消耗统计

## 国产替代方案

项目支持以下国产模型/数据库切换：

| 类型 | 当前选择 | 替代方案 |
|------|---------|---------|
| LLM | 通义千问 qwen-max | 智谱 GLM-4、DeepSeek V3、Kimi K2.5 |
| Embedding | text-embedding-v3 | BGE-large-zh、BGE-m3 |
| 向量库 | Milvus | 阿里云 OpenSearch、Lindorm |

## License

MIT
