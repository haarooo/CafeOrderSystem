package com.example.cafeordersystem.reply.kafka;

import com.example.cafeordersystem.reply.dto.ReviewReplyQueryRequestEvent;
import com.example.cafeordersystem.reply.dto.ReviewReplyQueryResultEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ReviewReplyKafkaClient {

    private final ReviewReplyQueryRequestProducer requestProducer;

    private final Map<String, CompletableFuture<ReviewReplyQueryResultEvent>> pendingRequests =
            new ConcurrentHashMap<>();

    public ReviewReplyQueryResultEvent requestReply(Long customerReviewId) {
        try {
            String requestId = UUID.randomUUID().toString();

            ReviewReplyQueryRequestEvent event =
                    ReviewReplyQueryRequestEvent.builder()
                            .requestId(requestId)
                            .customerReviewId(customerReviewId)
                            .build();

            CompletableFuture<ReviewReplyQueryResultEvent> future =
                    new CompletableFuture<>();

            pendingRequests.put(requestId, future);

            requestProducer.send(event);

            return future.get(10, TimeUnit.SECONDS);

        } catch (Exception e) {
            throw new RuntimeException("답글 조회 결과 수신 실패", e);
        }
    }

    public void complete(ReviewReplyQueryResultEvent resultEvent) {
        CompletableFuture<ReviewReplyQueryResultEvent> future =
                pendingRequests.remove(resultEvent.getRequestId());

        if (future != null) {
            future.complete(resultEvent);
        }
    }
}