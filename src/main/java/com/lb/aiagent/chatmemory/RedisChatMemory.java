package com.lb.aiagent.chatmemory;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.lb.aiagent.model.dto.RedisMessageDTO;
import com.lb.aiagent.utils.JacksonUtil;
import com.lb.aiagent.utils.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.model.Media;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisChatMemory implements ChatMemory {

    private final StringRedisTemplate stringRedisTemplate = SpringUtil.getBean(StringRedisTemplate.class);

    public static final String CHAT_MEMORY_KEY = "CHAT:MEMORY:KEY:CONVERSATIONID:";

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> conversationMessages = this.getOrCreateConversation(conversationId);
        conversationMessages.addAll(messages);
        this.saveConversation(conversationId, conversationMessages);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        List<Message> messages = this.getOrCreateConversation(conversationId);
        return messages.subList(Math.max(messages.size() - lastN, 0), messages.size());
    }

    @Override
    public void clear(String conversationId) {
        stringRedisTemplate.delete(CHAT_MEMORY_KEY + conversationId);
    }

    private List<Message> getOrCreateConversation(String conversationId) {
        String messageStr = stringRedisTemplate.opsForValue().get(CHAT_MEMORY_KEY + conversationId);
        if (StrUtil.isBlank(messageStr)) {
            return new ArrayList<>();
        }
        List<RedisMessageDTO> dtoList = JacksonUtil.toList(messageStr, RedisMessageDTO.class);
        List<Message> messages = new ArrayList<>(dtoList.size());
        for (RedisMessageDTO dto : dtoList) {
            Message message = this.coverMessage(dto.getMessageType(), JacksonUtil.toJsonString(dto.getMessage()));
            messages.add(message);
        }
        return messages;
    }

    private void saveConversation(String conversationId, List<Message> messages) {
        List<RedisMessageDTO> dtoList = this.messageCoverDTO(messages);
        stringRedisTemplate.opsForValue().set(CHAT_MEMORY_KEY + conversationId,
                JacksonUtil.toJsonString(dtoList),
                60 * 60, TimeUnit.SECONDS);
    }

    private List<RedisMessageDTO> messageCoverDTO(List<Message> messages) {
        List<RedisMessageDTO> list = new ArrayList<>(messages.size());
        for (Message message : messages) {
            RedisMessageDTO dto = new RedisMessageDTO();
            dto.setMessage(message);
            dto.setMessageType(message.getMessageType().name());
            list.add(dto);
        }
        return list;
    }

    public <T extends Message> Class<T> getMessageClass(String messageType) {
        if (MessageType.USER.name().equals(messageType)) {
            return (Class<T>) UserMessage.class;
        } else if (MessageType.SYSTEM.name().equals(messageType)) {
            return (Class<T>)SystemMessage.class;
        } else if (MessageType.ASSISTANT.name().equals(messageType)) {
            return (Class<T>)AssistantMessage.class;
        } else if (MessageType.TOOL.name().equals(messageType)) {
            return (Class<T>)ToolResponseMessage.class;
        } else {
            throw new IllegalArgumentException("Invalid message type: " + messageType);
        }
    }

    public Message coverMessage(String messageType, String messageStr) {
        Map map = JacksonUtil.toObject(messageStr, Map.class);
        String content = MapUtil.get(map, "content", String.class);
        List<Media> medias = MapUtil.getList(map, "media", Media.class);
        Map<String, Object> metadata = MapUtil.get(map, "metadata", Map.class);
        if (MessageType.USER.name().equals(messageType)) {
            return new UserMessage(MessageType.USER, content, medias, metadata);
        } else if (MessageType.SYSTEM.name().equals(messageType)) {
            return new SystemMessage(content);
        } else if (MessageType.ASSISTANT.name().equals(messageType)) {
            return new AssistantMessage(content, metadata);
        } else if (MessageType.TOOL.name().equals(messageType)) {
            return new ToolResponseMessage(List.of(), metadata);
        } else {
            throw new IllegalArgumentException("Invalid message type: " + messageType);
        }
    }
}
