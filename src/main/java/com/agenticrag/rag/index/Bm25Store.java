package com.agenticrag.rag.index;

import org.springframework.stereotype.Component;

/**
 * BM25 存储（SQLite FTS5，本地文件 ./data/bm25.db，首次启动自动建表）
 * <p>
 * 计划方法：
 * - void saveChunks(String docName, List&lt;Chunk&gt; chunks)         写入（bigram 化后 insert）
 * - List&lt;VectorSearchResult&gt; search(String query, int topK)      关键词检索（查询词同样 bigram 化）
 * <p>
 * 中文关键设计：FTS5 unicode61 对中文按空白切词会失效，入库与查询两侧统一做 bigram 分词
 * （连续 2 个汉字间插空格：你好世界 → 你好 好世 世界）
 */
@Component
public class Bm25Store {
}
