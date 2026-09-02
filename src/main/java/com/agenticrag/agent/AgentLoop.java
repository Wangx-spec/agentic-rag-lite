package com.agenticrag.agent;

/**
 * Agent 循环（M3 实现思考-行动-观察状态机）
 * <p>
 * 计划流程：THINKING →（需要工具？）→ ACTING → OBSERVING → THINKING → ... → FINAL
 * 每轮把「消息 + 可用工具 Schema」喂给 LLM，模型返回
 * 调用工具(name,args) 或 最终回答；工具输出作为观察结果拼回上下文。
 * <p>
 * M1 阶段聊天链路不经过本类（直接 LLM 流式对话），接口先行占位。
 */
public class AgentLoop {
}
