package com.example.cafeordersystem.global.outbox;

import com.example.cafeordersystem.global.event.EventEnvelope;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 도메인 서비스에서 outbox 이벤트를 저장할 때 사용하는 서비스.
 *
 * 이 서비스는 반드시 도메인 저장 트랜잭션 안에서 호출되어야 한다.
 * 예:
 * - review 저장
 * - outbox 저장
 * 두 작업이 같은 @Transactional 안에서 같이 commit 되어야 한다.
 */
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxMapper outboxMapper;
    private final ObjectMapper objectMapper;

    public void saveEvent(
            String topic,
            String eventType,
            String aggregateType,
            String aggregateId,
            JsonNode payload
    ) {
        try {
            String eventId = UUID.randomUUID().toString();

            EventEnvelope envelope = EventEnvelope.builder()
                    .eventId(eventId)
                    .eventType(eventType)
                    .eventVersion(1)
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .occurredAt(LocalDateTime.now().toString())
                    .payload(payload)
                    .build();

            String envelopeJson = objectMapper.writeValueAsString(envelope);

            OutboxEvent event = OutboxEvent.builder()
                    .eventId(eventId)
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .topic(topic)
                    .kafkaKey(aggregateId)
                    .payload(envelopeJson)
                    .status("NEW")
                    .retryCount(0)
                    .build();

            outboxMapper.insertOutboxEvent(event);

        } catch (Exception e) {
            throw new RuntimeException("outbox 이벤트 저장 실패", e);
        }
    }
}