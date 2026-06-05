package com.example.cafeordersystem.reply.kafka;

import com.example.cafeordersystem.reply.dto.ReviewReplyQueryResultEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewReplyQueryResultConsumer {

    private final ObjectMapper objectMapper;
    private final ReviewReplyKafkaClient replyKafkaClient;

    @KafkaListener(
            topics = "review-reply-query-result",
            groupId = "customer-reply-query-service"
    )
    public void consume(String message) {
        try {
            log.info("📩 답글 조회 결과 수신: {}", message);

            ReviewReplyQueryResultEvent result =
                    objectMapper.readValue(message, ReviewReplyQueryResultEvent.class);

            replyKafkaClient.complete(result);

        } catch (Exception e) {
            log.error("답글 조회 결과 처리 실패", e);
        }
    }
}