package com.lb.aiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

/**
 * 聊天记忆建议
 */
@Slf4j
public class MyMessageChatMemoryAdvisor extends AbstractChatMemoryAdvisor<ChatMemory> {

	public MyMessageChatMemoryAdvisor(ChatMemory chatMemory) {
		super(chatMemory);
	}

	public MyMessageChatMemoryAdvisor(ChatMemory chatMemory, String defaultConversationId, int defaultChatMemoryRetrieveSize, boolean protectFromBlocking) {
		super(chatMemory, defaultConversationId, defaultChatMemoryRetrieveSize, protectFromBlocking);
	}

	public MyMessageChatMemoryAdvisor(ChatMemory chatMemory, String defaultConversationId, int defaultChatMemoryRetrieveSize, boolean protectFromBlocking, int order) {
		super(chatMemory, defaultConversationId, defaultChatMemoryRetrieveSize, protectFromBlocking, order);
	}

	@Override
	public int getOrder() {
		return 100;
	}

	@Override
	public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {

		advisedRequest = this.before(advisedRequest);

		AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);

		this.observeAfter(advisedResponse);

		return advisedResponse;
	}

	@Override
	public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {

		Flux<AdvisedResponse> advisedResponses = this.doNextWithProtectFromBlockingBefore(advisedRequest, chain,
				this::before);

		return new MessageAggregator().aggregateAdvisedResponse(advisedResponses, this::observeAfter);
	}

	private AdvisedRequest before(AdvisedRequest request) {
		log.info("before " + request.messages());
		return request;
	}

	private void observeAfter(AdvisedResponse advisedResponse) {
		log.info("after " + advisedResponse.response().getResult().getOutput().toString());
	}
}