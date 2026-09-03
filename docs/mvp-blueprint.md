# 自研 Agentic RAG 平台 — MVP 方案（新项目起点）

> 定位：面向企业知识库问答的 Java Agentic RAG 平台「简化版」，从零自研，聚焦核心链路做扎实。
> 目标：一个能跑通、能演示、能评测、面试能逐模块讲清楚的最小闭环。
> 原则：Agent 循环 + RAG 检索是骨架（不省）；为规模/并发/容错而生的能力全部砍掉（主动精简）。

---

## 一、范围（做 / 不做）

**做（核心链路，做扎实）**
- Agent 核心引擎：思考-行动-观察循环 + 工具注册/校验/调用 + 消息流转（自研状态机，不依赖 LangChain/LangGraph）
- RAG：PDF/文本解析 → 分块 → 向量化入库 → 向量检索 + BM25 → RRF 融合 → 重排 → 上下文组装
- 会话记忆：最近 N 轮（内存）+ Redis 简易长期摘要
- 流式输出（SSE）+ 回答溯源（引用 chunk）
- RESTful API + 配置化 + 异常/超时控制
- 评测脚本：回答准确率 + 工具调用成功率
- Dockerfile + 最小 K8s Deployment + README（架构图/部署/接口/测试）

**不做（主动精简，面试可讲「为什么砍」）**
- 不接 MCP 协议（自研 `Tool` 接口，提参用模型 function calling）
- 不做 ES/知识图谱/联网搜索（BM25 用 SQLite FTS5，检索仅向量+BM25 两通道）
- 不做三态熔断/首包探测/候选切换（单模型 + 超时 + 重试）
- 不做公平排队/Lua/RocketMQ（简单信号量限并发）
- 不做全链路 Trace（关键节点日志）
- 多 Agent 只做最简主从（拆子问题 → 子 Agent → 汇总），不做共识/多轮辩论
- 不做 Docker 沙箱、不做管理后台前端

---

## 二、技术栈

| 层 | 选型 | 理由 |
|---|---|---|
| 语言/框架 | Java 17 + Spring Boot 3.x（单 Maven 模块） | 用户熟悉；单模块降低骨架复杂度 |
| LLM 调用 | 自研 OpenAI 兼容 HTTP 客户端 + SSE 解析 | 体现「原生实现」，面试讲得清协议细节 |
| Embedding | OpenAI 兼容接口（在线，如硅基流动） | 与 LLM 同一套客户端 |
| 向量库 | Qdrant（Docker 单容器，gRPC 客户端；VectorStore 接口可插拔，内存实现兜底） | 轻于 Milvus（无 etcd/MinIO）；payload 免回表；2026-09-03 由 pgvector 改定 |
| BM25 | SQLite FTS5（JDBC 内置，零外部服务） | 替代 ES，一条依赖搞定关键词检索 |
| 记忆 | Redis（可选，存摘要；会话内 N 轮用内存） | 轻量 |
| 构建/部署 | Maven + Dockerfile + K8s Deployment（最小） | 社招工程化必备 |
| 评测 | Java 评测类 + 评测集 JSON（可选 Python 脚本） | 回答准确率 + 工具调用成功率 |

---

## 三、模块划分（package 分层）

单模块，按职责分包：

```
com.yourapp.agenticrag/
├── agent/          # Agent 核心引擎
│   ├── AgentLoop.java          # 思考-行动-观察主循环
│   ├── AgentState.java         # 状态机枚举（THINKING/ACTING/OBSERVING/FINAL）
│   └── AgentContext.java       # 单次任务上下文（消息、工具结果、轮次）
├── tool/           # 工具体系
│   ├── Tool.java               # 工具接口：name/description/schema/execute
│   ├── ToolRegistry.java       # 注册表
│   ├── ToolSchema.java         # 参数 Schema + 校验
│   └── tools/                  # 内置工具（如 SearchKnowledgeBaseTool）
├── rag/            # RAG 能力
│   ├── ingest/                 # 解析（PDF/文本）、分块（Fixed/StructureAware）、入库
│   ├── index/                  # 向量索引(Qdrant，VectorStore 接口) + BM25(FTS5)
│   ├── retrieve/               # 双通道检索 + RRF 融合 + 重排
│   └── eval/                   # 检索质量评估
├── llm/            # 模型层
│   ├── LlmClient.java          # OpenAI 兼容 chat/embedding
│   ├── SseParser.java          # 流式解析
│   └── LlmConfig.java          # 模型/超时/重试配置
├── memory/         # 会话记忆
│   └── ConversationMemory.java # N 轮窗口 + Redis 摘要
├── api/            # 对外接口
│   ├── ChatController.java     # 问答接口（SSE）
│   ├── IngestController.java   # 文档入库接口
│   └── dto/
├── eval/           # 评测闭环
│   ├── AnswerAccuracyEval.java # 回答准确率
│   └── ToolSuccessEval.java    # 工具调用成功率
└── config/         # 配置化（application.yaml + @ConfigurationProperties）
```

---

## 四、核心链路设计

### 1. Agent 状态机（自研，精简）

```
THINKING → (需要工具?) → ACTING → OBSERVING → THINKING → ... → FINAL
              ↓ 不需要工具
            FINAL（直接回答）
```

- `AgentLoop`：循环最多 N 轮（默认 5），每轮把「消息 + 可用工具 Schema」喂给 LLM
- 模型返回：`action=调用工具(name,args)` 或 `action=最终回答`
- 工具输出作为观察结果（OBSERVING）拼回上下文，进入下一轮 THINKING
- 参数校验：调工具前用 `ToolSchema` 校验必填与类型，失败则把错误反馈给模型重试

### 2. RAG 检索流程

```
query → 向量检索(topK) + BM25(topK) → RRF 融合 → 重排 → 取 top N 组装 context
```

- 分块：先做固定大小 + 结构化（按标题/段落）两种，重叠窗口保留上下文
- RRF：`score = Σ 1/(k + rank)`，两通道结果融合
- 重排：先用简单方案（embedding 相似度兜底），预留 Rerank 接口后续接重排模型

### 3. 评测（差异化亮点）

- **回答准确率**：评测集（问题 + 参考答案），跑完对比（精确匹配 / 关键词重叠 / 可选 LLM 判定）
- **工具调用成功率**：构造「必须调工具才能答」的问题集，统计「正确调用了工具且参数正确」的比例
- 输出：报告 + 每题明细

---

## 五、里程碑（M1→M6）

| 里程碑 | 内容 | 验收 |
|---|---|---|
| M1 骨架 | 项目搭建 + LLM 客户端 + 简单问答 | 能调通 LLM 得到回答 |
| M2 RAG | 解析/分块/入库 + 双通道检索 + 融合 | 上传 PDF 能检索出相关 chunk |
| M3 Agent | 状态机 + 工具注册/校验/调用 | 能调工具完成多轮任务 |
| M4 体验 | SSE 流式 + 溯源 + 会话记忆 | 流式输出带引用来源 |
| M5 多 Agent | 主从最简版（拆子问题+汇总） | 复合问题拆解并聚合 |
| M6 收口 | 评测脚本 + Dockerfile + K8s + README | 一键部署 + 评测报告 |

---

## 六、关键取舍（面试可主动讲）

1. 「单模块而非多模块」——MVP 规模用分包即可，避免过早抽象
2. 「SQLite FTS5 代替 ES」——BM25 语义相同，省一个中间件；需要分布式再换
3. 「手写 OpenAI 客户端而非 Spring AI」——掌握协议细节，不黑盒
4. 「砍熔断/排队/事务消息」——单人演示无高并发，保留超时+重试足够
