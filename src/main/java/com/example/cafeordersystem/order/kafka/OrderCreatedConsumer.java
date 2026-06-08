package com.example.cafeordersystem.order.kafka;

import com.example.cafeordersystem.global.event.EventEnvelope;
import com.example.cafeordersystem.global.event.ProcessedEventService;
import com.example.cafeordersystem.order.dto.OrderCreatedPayload;
import com.example.cafeordersystem.order.read.ReviewableOrderReadMapper;
import com.example.cafeordersystem.order.read.ReviewableOrderReadRow;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사장 서버에서 발행한 order.created 이벤트를 받아
 * 구매 서버 reviewable_order_read를 갱신한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final ObjectMapper objectMapper;
    private final ProcessedEventService processedEventService;
    private final ReviewableOrderReadMapper reviewableOrderReadMapper;

    @Transactional
    @KafkaListener(
            topics = "order.created",
            groupId = "customer-reviewable-order-service"
    )
    public void consume(String message) {
        try {
            EventEnvelope event = objectMapper.readValue(message, EventEnvelope.class);

            boolean firstProcess = processedEventService.tryMarkProcessed(
                    event.getEventId(),
                    event.getEventType()
            );

            if (!firstProcess) {
                log.info("중복 주문 이벤트 skip eventId={}", event.getEventId());
                return;
            }

            OrderCreatedPayload payload =
                    objectMapper.treeToValue(event.getPayload(), OrderCreatedPayload.class);

            ReviewableOrderReadRow row = new ReviewableOrderReadRow();
            row.setOrderId(payload.getOrderId());
            row.setOrderPrice(payload.getOrderPrice());
            row.setCreatedAt(payload.getCreatedAt());
            row.setReviewWritten(false);

            reviewableOrderReadMapper.upsertOrder(row);

            log.info("✅ reviewable_order_read upsert 완료 orderId={}", payload.getOrderId());

        } catch (Exception e) {
            log.error("주문 생성 이벤트 처리 실패 message={}", message, e);
            throw new RuntimeException(e);
        }
    }
}