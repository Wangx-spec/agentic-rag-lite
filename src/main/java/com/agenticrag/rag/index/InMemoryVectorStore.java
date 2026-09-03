package com.agenticrag.rag.index;

import org.springframework.stereotype.Component;

/**
 * 内存向量存储实现（余弦相似度暴力检索）
 * <p>
 * 用途：零依赖单测与演示兜底；按 rag.vector.type=memory 时装配。
 * 数据量小场景性能足够，重启即失（不入库持久化）。
 */
@Component
public class InMemoryVectorStore implements VectorStore {
}
