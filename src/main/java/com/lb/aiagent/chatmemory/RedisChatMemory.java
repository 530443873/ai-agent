package com.lb.aiagent.chatmemory;

import cn.hutool.core.util.StrUtil;
import com.lb.aiagent.utils.JacksonUtil;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@AllArgsConstructor
@NoArgsConstructor
public class RedisChatMemory implements ChatMemory {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private String BASE_DIR;

    public static final String CHAT_MEMORY_KEY = "CHAT:MEMORY:KEY:CONVERSATIONID:";

    public RedisChatMemory(String dir) {
        this.BASE_DIR = dir;
    }

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
        return JacksonUtil.toList(messageStr, Message.class);
    }

    private void saveConversation(String conversationId, List<Message> messages) {
        stringRedisTemplate.opsForValue().set(CHAT_MEMORY_KEY + conversationId, JacksonUtil.toJsonString(messages),
                60 * 60, TimeUnit.SECONDS);
    }
}
