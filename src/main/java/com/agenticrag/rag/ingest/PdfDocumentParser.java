package com.agenticrag.rag.ingest;

import org.springframework.stereotype.Component;

/**
 * PDF 文档解析器（PDFBox 3.x 实现）
 * <p>
 * 实现要点：PDFBox 逐页抽取文本，处理分页截断（页间补空白/换行）；supports 匹配 .pdf
 */
@Component
public class PdfDocumentParser implements DocumentParser {
}
