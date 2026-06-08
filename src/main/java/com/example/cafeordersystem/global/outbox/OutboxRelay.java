package com.example.cafeordersystem.global.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * outbox 테이블의 NEW 이벤트를 Kafka로 발행하는 relay.
 *
 * MVP에서는 polling scheduler 방식으로 충분하다.
 * 운영 환경에서는 Debezium CDC 기반 outbox 패턴으로 확장할 수 있다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

    private static final int BATCH_SIZE = 50;

    private final OutboxMapper outboxMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 3000)
    public void publishNewEvents() {
        List<OutboxEvent> events = outboxMapper.findNewEvents(BATCH_SIZE);

        if (events.isEmpty()) {
            return;
        }

        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(
                        event.getTopic(),
                        event.getKafkaKey(),
                        event.getPayload()
                ).get();

                outboxMapper.markAsSent(event.getId());

                log.info("✅ outbox 이벤트 발행 성공 eventId={}, topic={}",
                        event.getEventId(), event.getTopic());

            } catch (Exception e) {
                log.error("❌ outbox 이벤트 발행 실패 eventId={}, topic={}",
                        event.getEventId(), event.getTopic(), e);

                outboxMapper.markAsFailed(
                        event.getId(),
                        e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()
                );
            }
        }
    }
}