package com.agenticrag.rag.ingest;

import org.springframework.stereotype.Component;

/**
 * 结构化分块器：按标题/段落边界切块；段落超长时回退 FixedChunker 兜底
 * <p>
 * 实现要点：识别 Markdown 标题（#）/ 空行段落边界，标题跟随其所属段落
 */
@Component
public class StructureChunker implements Chunker {
}
