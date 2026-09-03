package com.agenticrag.rag.ingest;

import org.springframework.stereotype.Component;

/**
 * 固定长度分块器：按 chunkSize 切块，块间保留 overlap 重叠（默认 500 字 / overlap 50，读 RagProperties）
 */
@Component
public class FixedChunker implements Chunker {
}
