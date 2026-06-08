package com.example.cafeordersystem.global.outbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * outbox 테이블 1행을 표현하는 DTO.
 *
 * 구매 서버에서 review.created 같은 이벤트를 바로 Kafka로 보내지 않고,
 * 먼저 같은 DB 트랜잭션 안에서 outbox에 저장한다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    private Long id;

    private String eventId;

    private String aggregateType;

    private String aggregateId;

    private String eventType;

    private String topic;

    private String kafkaKey;

    private String payload;

    private String status;

    private Integer retryCount;

    private String lastError;

    private String createdAt;

    private String sentAt;
}