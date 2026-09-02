package com.agenticrag.api;

import com.agenticrag.config.LlmProperties;
import com.agenticrag.llm.LlmClient;
import com.agenticrag.llm.dto.ChatMessage;
import com.agenticrag.memory.ConversationMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 聊天接口（SSE 流式）
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private static final String SYSTEM_PROMPT = "你是一个有帮助的 AI 助手。回答保持准确、简洁。";

    private final LlmClient llmClient;
    private final ConversationMemory memory;
    private final LlmProperties llmProperties;

    /** 聊天执行线程池：与 Tomcat 工作线程隔离 */
    private final ExecutorService chatExecutor = Executors.newCachedThreadPool();

    public record ChatRequest(String sessionId, String message) {
    }

    /**
     * 流式对话：SSE 事件 delta（增量文本）→ done / error
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody ChatRequest request) {
        String sessionId = (request.sessionId() == null || request.sessionId().isBlank())
                ? UUID.randomUUID().toString()
                : request.sessionId();
        String question = request.message() == null ? "" : request.message().trim();

        SseEmitter emitter = new SseEmitter(120_000L);

        if (question.isEmpty()) {
            sendAndComplete(emitter, "error", "{\"message\":\"message 不能为空\"}");
            return emitter;
        }
        if (!llmProperties.isConfigured()) {
            sendAndComplete(emitter, "error",
                    "{\"message\":\"LLM 未配置：请设置环境变量 LLM_API_KEY，并在 application.yaml 配置 llm.base-url / llm.model\"}");
            return emitter;
        }

        chatExecutor.submit(() -> {
            try {
                memory.append(sessionId, ChatMessage.user(question));
                List<ChatMessage> messages = new ArrayList<>();
                messages.add(ChatMessage.system(SYSTEM_PROMPT));
                messages.addAll(memory.load(sessionId, llmProperties.getMemoryRounds() * 2));

                StringBuilder answer = new StringBuilder();
                llmClient.chatStream(messages, delta -> {
                    answer.append(delta);
                    sendQuietly(emitter, "delta", delta);
                });

                memory.append(sessionId, ChatMessage.assistant(answer.toString()));
                sendAndComplete(emitter, "done", "{\"sessionId\":\"" + sessionId + "\"}");
            } catch (Exception e) {
                log.error("聊天处理失败, sessionId: {}", sessionId, e);
                sendAndComplete(emitter, "error", "{\"message\":\"" + escape(e.getMessage()) + "\"}");
            }
        });
        return emitter;
    }

    /**
     * 健康检查：服务状态 + LLM 配置是否就绪
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "llmConfigured", llmProperties.isConfigured()
        );
    }

    // ==================== 内部工具 ====================

    private void sendQuietly(SseEmitter emitter, String event, String data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(data == null ? "" : data));
        } catch (IOException | IllegalStateException e) {
            // 客户端断开：忽略
        }
    }

    private void sendAndComplete(SseEmitter emitter, String event, String data) {
        sendQuietly(emitter, event, data);
        try {
            emitter.complete();
        } catch (Exception ignored) {
        }
    }

    private static String escape(String text) {
        if (text == null) {
            return "unknown error";
        }
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}
