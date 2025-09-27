package com.lb.aiagent.advisor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.lb.aiagent.exception.BusinessException;
import com.lb.aiagent.service.ProhibitedWordsService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.MessageAggregator;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 违禁词拦截器
 */
@Slf4j
@Component
public class ProhibitedWordsAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    private volatile List<String> prohibitedWords;

    // 构建正则表达式模式用于匹配违禁词
    private Pattern pattern;

    public void initProhibitedWords() {
        ProhibitedWordsService prohibitedWordsService = SpringUtil.getBean(ProhibitedWordsService.class);
        prohibitedWords = prohibitedWordsService.getAllWords();
        StringBuilder patternBuilder = new StringBuilder();
        if (CollUtil.isEmpty(prohibitedWords)) {
            this.pattern = Pattern.compile(patternBuilder.toString(), Pattern.CASE_INSENSITIVE);
            return;
        }
        for (int i = 0; i < prohibitedWords.size(); i++) {
            if (i > 0) {
                patternBuilder.append("|");
            }
            patternBuilder.append(Pattern.quote(prohibitedWords.get(i)));
        }
        this.pattern = Pattern.compile(patternBuilder.toString(), Pattern.CASE_INSENSITIVE);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 10;
    }

    /**
     * 检查文本是否包含违禁词
     * @param text 待检查文本
     * @return true表示包含违禁词，false表示不包含
     */
    private boolean containsProhibitedWords(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return pattern.matcher(text).find();
    }

    /**
     * 替换文本中的违禁词
     * @param text 原始文本
     * @return 替换后的文本
     */
    private String replaceProhibitedWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String result = text;
        for (String word : prohibitedWords) {
            result = result.replaceAll("(?i)" + Pattern.quote(word), "*".repeat(word.length()));
        }
        return result;
    }

    /**
     * 在请求前检查违禁词
     * @param request 原始请求
     * @return 处理后的请求
     * @throws SecurityException 如果包含违禁词则抛出异常
     */
    private AdvisedRequest before(AdvisedRequest request) {
        // 检查用户输入是否包含违禁词
        if (containsProhibitedWords(request.userText())) {
            log.warn("Request contains prohibited words: {}", request.userText());
            throw new BusinessException("请求包含违禁词");
        }

        // 检查系统文本是否包含违禁词
        if (containsProhibitedWords(request.systemText())) {
            log.warn("System text contains prohibited words: {}", request.systemText());
            throw new BusinessException("系统文本包含违禁词");
        }

        log.debug("Request passed prohibited words check");
        return request;
    }

    /**
     * 在响应后检查违禁词并修改响应内容
     * @param advisedResponse 原始响应
     * @return 修改后的响应
     */
    private AdvisedResponse observeAfterAndModify(AdvisedResponse advisedResponse) {
        String responseText = advisedResponse.response().getResult().getOutput().getText();

        if (containsProhibitedWords(responseText)) {
            log.warn("Response contains prohibited words: {}", responseText);

            // 替换违禁词
            String cleanedText = replaceProhibitedWords(responseText);

            // 创建修改后的响应
            return createModifiedResponse(advisedResponse, cleanedText);
        }

        // 如果没有违禁词，返回原始响应
        return advisedResponse;
    }

    /**
     * 创建修改后的响应对象
     * @param originalResponse 原始响应
     * @param modifiedText 修改后的文本
     * @return 新的响应对象
     */
    private AdvisedResponse createModifiedResponse(AdvisedResponse originalResponse, String modifiedText) {
        // 使用构建器创建新的ChatResponse
        ChatResponse modifiedChatResponse = ChatResponse.builder()
                .from(originalResponse.response())
                .generations(List.of(new Generation(new AssistantMessage(modifiedText))))
                .build();

        // 创建新的AdvisedResponse
        return new AdvisedResponse(
                modifiedChatResponse,
                originalResponse.adviseContext()
        );
    }

    @Override
    public AdvisedResponse aroundCall(@NotNull AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        this.initProhibitedWords();
        // 请求前检查
        before(advisedRequest);

        // 继续执行调用链
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);

        // 响应后检查
        return observeAfterAndModify(advisedResponse);
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(@NotNull AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        // 请求前检查
        before(advisedRequest);

        // 继续执行流式调用链
        Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);

        // 对流式响应进行聚合处理
        return new MessageAggregator().aggregateAdvisedResponse(advisedResponses, this::observeAfterAndModify);
    }
}
