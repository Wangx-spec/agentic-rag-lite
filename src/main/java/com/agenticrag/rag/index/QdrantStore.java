package com.agenticrag.rag.index;

import org.springframework.stereotype.Component;

/**
 * Qdrant 向量存储实现（官方 gRPC 客户端 io.qdrant:client，端口 6334）
 * <p>
 * 实现要点：
 * - 启动时确保 collection 存在（agentic_rag_chunks，Cosine 距离，HNSW m=16/ef_construct=100，维度读配置）
 * - point id = PG chunk id；payload = {content, docName, seq, documentId}，检索免回表
 * - 按 rag.vector.type=qdrant 时装配（条件装配）
 */
@Component
public class QdrantStore implements VectorStore {
}
