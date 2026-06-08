package com.example.cafeordersystem.global.event;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 컨슈머 멱등 처리를 담당한다.
 *
 * Kafka는 같은 메시지를 두 번 전달할 수 있으므로,
 * eventId를 processed_event 테이블에 UNIQUE로 저장하고
 * 이미 처리한 eventId면 비즈니스 로직을 실행하지 않는다.
 */
@Service
@RequiredArgsConstructor
public class ProcessedEventService {

    private final ProcessedEventMapper processedEventMapper;

    public boolean tryMarkProcessed(String eventId, String eventType) {
        try {
            processedEventMapper.insertProcessedEvent(eventId, eventType);
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        }
    }
}