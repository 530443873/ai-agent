package com.lb.aiagent.model.dto;

import lombok.Data;
import org.springframework.ai.chat.messages.MessageType;

@Data
public class RedisMessageDTO {
    /**
     * 元数据类型
     * @see MessageType
     */
    private String messageType;

    /**
     * 元数据
     */
    private Object message;
}
