# Windows 环境安装指南

本文档指导您在 Windows 系统上运行 modeAI 企业级 RAG 智能知识库问答系统。

## 一、环境要求

| 软件 | 版本要求 | 用途 |
|------|---------|------|
| JDK | 21+ | 后端运行环境 |
| Docker Desktop | 最新版 | 运行 MySQL、Redis、Milvus、RabbitMQ |
| Node.js | 18+ | 前端构建 |
| Git | 最新版 | 版本管理 |

> **硬件建议**: 内存 16GB+，磁盘 20GB+ 可用空间。Docker Desktop 建议分配 8GB 内存。

## 二、安装步骤

### 2.1 安装 JDK 21

**推荐方式**: 下载 Eclipse Temurin (AdoptOpenJDK)

1. 访问 https://adoptium.net/temurin/releases/?version=21
2. 选择操作系统: Windows，架构: x64，包类型: JDK
3. 下载 `.msi` 安装包并运行安装
4. 安装完成后，打开 CMD 验证：

```cmd
java -version
```

输出应包含 `openjdk version "21.x.x"`。

**配置 JAVA_HOME（如安装程序未自动配置）**:
1. 右键"此电脑" → 属性 → 高级系统设置 → 环境变量
2. 新建系统变量: `JAVA_HOME` = `C:\Program Files\Eclipse Adoptium\jdk-21.x.x-hotspot`（根据实际安装路径）
3. 编辑 Path 变量，添加: `%JAVA_HOME%\bin`

### 2.2 安装 Docker Desktop

1. 访问 https://www.docker.com/products/docker-desktop/
2. 下载 Docker Desktop for Windows 并安装
3. 安装完成后启动 Docker Desktop
4. 确保在 Settings → General 中启用了 **Use the WSL 2 based engine**
5. 在 Settings → Resources → Advanced 中，将 Memory 调整为 **8GB+**

> **注意**: Docker Desktop 需要 Windows 10 64位 (版本 2004+) 或 Windows 11，且需启用 WSL2。如未启用，Docker Desktop 安装时会提示您安装。

验证安装：

```cmd
docker --version
docker-compose --version
```

### 2.3 安装 Node.js

1. 访问 https://nodejs.org/
2. 下载 **LTS** 版本（推荐 20.x 或 22.x）
3. 运行安装程序，勾选 "Automatically install the necessary tools"
4. 验证安装：

```cmd
node -v
npm -v
```

### 2.4 安装 Git

1. 访问 https://git-scm.com/download/win
2. 下载并安装，推荐选项保持默认
3. 验证安装：

```cmd
git --version
```

## 三、获取项目代码

```cmd
git clone https://github.com/dongzhaoyang9527-sys/modeAI.git
cd modeAI
```

## 四、配置环境变量

### 方式一: 使用 .env 文件（推荐）

1. 在项目根目录复制环境变量模板：

```cmd
copy .env.example .env
```

2. 编辑 `.env` 文件，填入您的实际配置：

```properties
# 必填: 阿里云 DashScope API Key（去 https://dashscope.console.aliyun.com/ 获取）
AI_DASHSCOPE_API_KEY=sk-your-actual-api-key

# 可选: JWT 密钥（生产环境请修改为随机字符串，至少 256 位）
JWT_SECRET=your-custom-jwt-secret-key-at-least-256-bits-long

# 其他配置使用默认值即可，除非您修改了中间件的端口或密码
```

### 方式二: 系统环境变量

1. 右键"此电脑" → 属性 → 高级系统设置 → 环境变量
2. 在"系统变量"中添加所需的变量

## 五、启动项目

### 5.1 启动基础设施（MySQL、Redis、Milvus、RabbitMQ）

```cmd
docker-compose up -d
```

等待所有服务启动完成（首次启动需要下载镜像，可能需要几分钟）：

```cmd
docker-compose ps
```

所有服务的状态应显示为 `Up`。

> **RabbitMQ 管理界面**: 浏览器访问 http://localhost:15672 ，账号密码: guest/guest

### 5.2 启动后端

```cmd
gradlew.bat :rag-app:bootRun
```

> **注意**: 首次启动时 Gradle 会下载依赖，可能需要 5-10 分钟。

后端启动成功后，可以访问：
- API 接口: http://localhost:8080
- API 文档 (Knife4j): http://localhost:8080/doc.html
- 健康检查: http://localhost:8080/actuator/health

### 5.3 启动前端（新开一个 CMD 窗口）

```cmd
cd frontend
npm install
npm run dev
```

前端启动成功后，浏览器访问: http://localhost:3000

### 5.4 登录系统

- 用户名: `admin`
- 密码: `admin123`

## 六、常见问题排查

### Q: Docker Desktop 启动失败
- 确保 BIOS 中已启用虚拟化 (Intel VT-x / AMD-V)
- 确保 WSL2 已安装: 打开 PowerShell (管理员) 运行 `wsl --install`
- 尝试重启 Docker Desktop

### Q: 端口被占用
- **3307 (Docker MySQL)**: 如果与本地 MySQL 端口冲突，已在 docker-compose.yml 中改为 3307
- **6379 (Redis)**: 检查是否已安装本地 Redis
- **8080 (后端)**: 修改 `application.yml` 中的 `server.port`
- **3000 (前端)**: 修改 `frontend/vite.config.ts` 中的 `server.port`

### Q: Gradle 下载依赖很慢
- 项目已配置了阿里云 Maven 镜像，如果仍然很慢，检查网络或尝试使用代理

### Q: 后端启动报错 "Unable to connect to Milvus"
- 确认 `docker-compose ps` 中 Milvus 服务状态为 Up
- Milvus 首次启动可能需要 1-2 分钟初始化，请耐心等待后重试

### Q: 前端 npm install 失败
- 尝试切换 npm 镜像: `npm config set registry https://registry.npmmirror.com`
- 删除 `node_modules` 和 `package-lock.json` 后重新 `npm install`

### Q: 登录后提示 "Token已过期" 或 401
- 检查系统时间是否正确（JWT 依赖系统时间）
- 检查 `.env` 中的 `JWT_SECRET` 是否已正确配置

## 七、停止项目

```cmd
# 停止后端: 在运行后端的 CMD 窗口按 Ctrl+C

# 停止前端: 在运行前端的 CMD 窗口按 Ctrl+C

# 停止基础设施
docker-compose down

# 停止并清除数据（慎用）
docker-compose down -v
```
