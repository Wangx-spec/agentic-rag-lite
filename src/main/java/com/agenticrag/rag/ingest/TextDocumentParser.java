package com.agenticrag.rag.ingest;

import org.springframework.stereotype.Component;

/**
 * 纯文本/Markdown 解析器
 * <p>
 * 实现要点：直接读流为 UTF-8 文本；supports 匹配 .txt / .md
 */
@Component
public class TextDocumentParser implements DocumentParser {
}
