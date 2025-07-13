package com.lb.aiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import org.springframework.stereotype.Component;

@Component
public class LangChainAiInvoke {

    public static void main(String[] args) {
        QwenChatModel qenChatModel = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .modelName("qwen-plus")
                .build();
        String chat = qenChatModel.chat("我是湖南第一靓仔，谁赞成，谁反对？");
        System.out.println(chat);
    }
}
