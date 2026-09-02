package com.agenticrag.memory;

import com.agenticrag.llm.dto.ChatMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 会话记忆（内存实现）：每个会话保留最近 N 轮消息
 * <p>
 * M1 为内存版；后续可替换为 Redis 持久化实现（接口不变）。
 */
@Component
public class ConversationMemory {

    private final Map<String, ConcurrentLinkedDeque<ChatMessage>> sessions = new ConcurrentHashMap<>();

    /** 记忆保留的消息条数（一问一答 = 2 条），由调用方按配置传入 */
    public List<ChatMessage> load(String sessionId, int maxMessages) {
        ConcurrentLinkedDeque<ChatMessage> history = sessions.get(sessionId);
        if (history == null) {
            return List.of();
        }
        List<ChatMessage> all = new ArrayList<>(history);
        if (all.size() <= maxMessages) {
            return all;
        }
        return all.subList(all.size() - maxMessages, all.size());
    }

    /** 追加一条消息到会话 */
    public void append(String sessionId, ChatMessage message) {
        ConcurrentLinkedDeque<ChatMessage> history =
                sessions.computeIfAbsent(sessionId, k -> new ConcurrentLinkedDeque<>());
        history.addLast(message);
    }

    /** 清空会话 */
    public void clear(String sessionId) {
        sessions.remove(sessionId);
    }
}
