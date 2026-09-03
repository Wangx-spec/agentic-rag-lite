package com.agenticrag.rag.index;

import org.springframework.stereotype.Component;

/**
 * Embedding 客户端（OpenAI 兼容 /embeddings，复用 LlmProperties 的 base-url/api-key）
 * <p>
 * 计划方法：
 * - float[] embed(String text)                    单条向量化
 * - List&lt;float[]&gt; embedBatch(List&lt;String&gt; texts)  批量向量化（默认 32/批，失败重试）
 */
@Component
public class EmbeddingClient {
}
