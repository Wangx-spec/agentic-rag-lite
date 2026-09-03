package com.agenticrag.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文档入库接口
 * <p>
 * 计划端点：
 * - POST /api/ingest     multipart 上传文档（pdf/txt/md），走 IngestService 完整入库，返回 {documentId, name, chunkCount}
 * - GET  /api/documents  文档列表（id/name/chunkCount/status）
 */
@RestController
@RequestMapping("/api")
public class IngestController {
}
