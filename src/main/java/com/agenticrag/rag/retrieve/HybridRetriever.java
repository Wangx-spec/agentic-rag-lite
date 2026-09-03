package com.agenticrag.rag.retrieve;

import org.springframework.stereotype.Component;

/**
 * 混合检索器：向量 + BM25 双通道 → RRF 融合 → top N
 * <p>
 * 计划方法：
 * - List&lt;RetrievedChunk&gt; retrieve(String query)   双通道各取 topK=10，RRF 融合（score = Σ 1/(60 + rank)）后取 top 5
 * <p>
 * 组装 context：每段前缀 [1][2]... 编号 + 文档名，供聊天链路注入 prompt
 */
@Component
public class HybridRetriever {
}
