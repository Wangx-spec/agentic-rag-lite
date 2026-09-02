# agentic-rag

从零自研的 Java Agentic RAG 平台（简化版）。

**当前进度：M1 骨架** —— LLM 流式对话 + Web 聊天界面，可运行。后续里程碑见 `dev-plan/自研AgenticRAG-MVP方案.md`（在 ragent 工作区）。

## 技术栈

Java 17 · Spring Boot 3.3 · 手写 OpenAI 兼容客户端（`java.net.http` + SSE 解析，不依赖任何 AI 框架）· 原生 HTML 前端（零构建）

## 快速运行（3 步）

```bash
# 1. 配置 API Key（OpenAI 兼容接口，任选一家）
export LLM_API_KEY=sk-xxx
# 可选：自定义服务与模型
# export LLM_BASE_URL=https://api.siliconflow.cn/v1
# export LLM_MODEL=Qwen/Qwen2.5-72B-Instruct

# 2. 启动（首次会自动下载 Maven 与依赖）
./mvnw spring-boot:run

# 3. 打开页面
# http://localhost:8080
```

## 接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/chat` | 流式对话（SSE：`delta` 增量 → `done` / `error`），body: `{"sessionId":"...","message":"..."}` |
| GET | `/api/health` | 健康检查 + LLM 配置状态 |

```bash
# curl 测试流式输出
curl -N -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"你好，介绍一下你自己"}'
```

## 项目结构（MVP 分层）

```
src/main/java/com/agenticrag/
├── agent/    # Agent 循环（M3：思考-行动-观察状态机）
├── tool/     # 工具接口与注册表（M3 启用）
├── rag/      # RAG：入库/索引/检索（M2）
├── llm/      # 手写 OpenAI 兼容客户端 + SSE 流式解析
├── memory/   # 会话记忆（内存版，最近 N 轮）
├── api/      # RESTful + SSE 接口
└── config/   # 配置化
src/main/resources/static/index.html   # 聊天页面（随后端一起跑，零构建）
```

## 里程碑

- [x] M1 骨架：LLM 流式对话 + 聊天界面
- [ ] M2 RAG：PDF 解析/分块/入库 + 向量+BM25 双通道检索 + RRF
- [ ] M3 Agent：状态机 + 工具注册/校验/调用循环
- [ ] M4 体验：SSE 流式 + 溯源 + 持久化记忆
- [ ] M5 多 Agent：主从最简版
- [ ] M6 收口：评测脚本 + Dockerfile + K8s + README
