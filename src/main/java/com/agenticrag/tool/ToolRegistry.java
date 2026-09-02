package com.agenticrag.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 工具注册表（M3 启用）：Agent 循环从这里取可用工具列表
 */
@Slf4j
@Component
public class ToolRegistry {

    private final Map<String, Tool> tools = new LinkedHashMap<>();

    public void register(Tool tool) {
        tools.put(tool.name(), tool);
        log.info("已注册工具: {}", tool.name());
    }

    public Optional<Tool> find(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    public Map<String, Tool> all() {
        return tools;
    }
}
