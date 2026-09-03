package com.agenticrag.rag.ingest;

import org.springframework.stereotype.Service;

/**
 * 入库编排服务：parse → chunk → embed → 写 PG 元数据 + Qdrant 向量 + SQLite FTS
 * <p>
 * 计划方法：
 * - Document ingest(String filename, InputStream in)   完整入库链路，返回文档元信息
 * <p>
 * 顺序约束：先写 PG 拿自增 chunk id，再以该 id 作为 Qdrant point id 写向量（两侧对齐）
 */
@Service
public class IngestService {
}
