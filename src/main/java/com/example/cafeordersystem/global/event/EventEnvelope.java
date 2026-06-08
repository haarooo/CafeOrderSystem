package com.example.cafeordersystem.global.event;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kafka로 주고받는 모든 도메인 이벤트의 공통 껍데기.
 *
 * 핵심 원칙:
 * - Kafka에는 조회 요청이 아니라 이미 일어난 사실(event)을 흘려보낸다.
 * - eventId는 컨슈머 멱등 처리를 위해 반드시 포함한다.
 * - aggregateId는 Kafka key로도 사용해서 같은 집계의 이벤트 순서를 지킨다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEnvelope {

    private String eventId;

    private String eventType;

    private Integer eventVersion;

    private String aggregateType;

    private String aggregateId;

    private String occurredAt;

    private JsonNode payload;
}