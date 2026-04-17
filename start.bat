@echo off
chcp 65001 >nul
title modeAI - 企业级RAG智能知识库问答系统

echo ============================================
echo   modeAI 启动脚本
echo ============================================
echo.

:: 检查 Docker
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Docker，请先安装 Docker Desktop
    echo 下载地址: https://www.docker.com/products/docker-desktop/
    pause
    exit /b 1
)

:: 检查 Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Java，请先安装 JDK 21
    echo 下载地址: https://adoptium.net/temurin/releases/?version=21
    pause
    exit /b 1
)

:: 检查 Node.js
node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Node.js，请先安装 Node.js 18+
    echo 下载地址: https://nodejs.org/
    pause
    exit /b 1
)

echo [1/4] 启动基础设施 (MySQL, Redis, Milvus, RabbitMQ)...
docker-compose up -d
echo.

echo [2/4] 等待基础设施就绪...
timeout /t 15 /nobreak >nul
echo.

echo [3/4] 启动后端服务 (端口 8080)...
start "modeAI Backend" cmd /k "gradlew.bat :rag-app:bootRun"
echo.

echo [4/4] 安装前端依赖并启动 (端口 3000)...
cd frontend
if not exist node_modules (
    echo 首次运行，安装前端依赖...
    call npm install
)
start "modeAI Frontend" cmd /k "npm run dev"
cd ..

echo.
echo ============================================
echo   启动完成!
echo ============================================
echo.
echo   后端 API:     http://localhost:8080
echo   API 文档:     http://localhost:8080/doc.html
echo   前端页面:     http://localhost:3000
echo   RabbitMQ管理: http://localhost:15672 (guest/guest)
echo.
echo   默认账号: admin / admin123
echo.
echo   按任意键关闭此窗口...
pause >nul
