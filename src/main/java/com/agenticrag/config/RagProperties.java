package com.agenticrag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RAG 配置（M2 填充字段）
 * <p>
 * 计划字段：
 * - chunkSize(500) / chunkOverlap(50)          分块参数
 * - topK(10) / finalTopN(5) / rrfK(60)         检索参数
 * - embeddingDim(1536)                          向量维度（与 embedding 模型一致）
 * - dataDir(./data)                             SQLite/本地数据目录
 * - vector.type(qdrant | memory)                向量存储装配选择
 * - vector.qdrant.host / port(6334) / collection(agentic_rag_chunks)
 */
@ConfigurationProperties(prefix = "rag")
public class RagProperties {
}
