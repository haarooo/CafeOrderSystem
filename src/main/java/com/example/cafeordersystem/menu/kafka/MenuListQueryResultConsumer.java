package com.example.cafeordersystem.menu.kafka;

import com.example.cafeordersystem.menu.dto.MenuListQueryResultEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * 사장 서버 → 구매 서버
 * 메뉴 목록 조회 결과 수신.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuListQueryResultConsumer {

    private final ObjectMapper objectMapper;
    private final MenuKafkaClient menuKafkaClient;

    @KafkaListener(
            topics = "menu-list-query-result",
            groupId = "customer-menu-list-service"
    )
    public void consume(String message) {
        try {
            log.info("📩 메뉴 목록 조회 결과 수신: {}", message);

            MenuListQueryResultEvent result =
                    objectMapper.readValue(message, MenuListQueryResultEvent.class);

            menuKafkaClient.complete(result);

        } catch (Exception e) {
            log.error("메뉴 목록 조회 결과 처리 실패", e);
        }
    }
}