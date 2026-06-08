package com.example.cafeordersystem.reply.kafka;

import com.example.cafeordersystem.global.event.EventEnvelope;
import com.example.cafeordersystem.global.event.ProcessedEventService;
import com.example.cafeordersystem.reply.dto.ReplyEventPayload;
import com.example.cafeordersystem.reply.read.ReplyReadMapper;
import com.example.cafeordersystem.reply.read.ReplyReadRow;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사장 서버에서 발행한 답글 이벤트를 받아 구매 서버 reply_read를 갱신한다.
 *
 * 이벤트:
 * - reply.created
 * - reply.updated
 * - reply.deleted
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReplyEventConsumer {

    private final ObjectMapper objectMapper;
    private final ProcessedEventService processedEventService;
    private final ReplyReadMapper replyReadMapper;

    @Transactional
    @KafkaListener(
            topics = {"reply.created", "reply.updated", "reply.deleted"},
            groupId = "customer-reply-read-service"
    )
    public void consume(String message) {
        try {
            EventEnvelope event = objectMapper.readValue(message, EventEnvelope.class);

            boolean firstProcess = processedEventService.tryMarkProcessed(
                    event.getEventId(),
                    event.getEventType()
            );

            if (!firstProcess) {
                log.info("중복 답글 이벤트 skip eventId={}", event.getEventId());
                return;
            }

            ReplyEventPayload payload =
                    objectMapper.treeToValue(event.getPayload(), ReplyEventPayload.class);

            if ("reply.deleted".equals(event.getEventType())) {
                replyReadMapper.markDeleted(payload.getCustomerReviewId());
                log.info("✅ reply_read 삭제 반영 customerReviewId={}", payload.getCustomerReviewId());
                return;
            }

            ReplyReadRow row = new ReplyReadRow();
            row.setCustomerReviewId(payload.getCustomerReviewId());
            row.setOrderId(payload.getOrderId());
            row.setReplyContent(payload.getReplyContent());
            row.setHasReply(true);
            row.setRepliedAt(payload.getRepliedAt());

            replyReadMapper.upsertReply(row);

            log.info("✅ reply_read upsert 완료 eventType={}, customerReviewId={}",
                    event.getEventType(), payload.getCustomerReviewId());

        } catch (Exception e) {
            log.error("답글 이벤트 처리 실패 message={}", message, e);
            throw new RuntimeException(e);
        }
    }
}