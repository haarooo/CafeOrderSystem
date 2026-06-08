package com.example.cafeordersystem.menu.kafka;

import com.example.cafeordersystem.global.event.EventEnvelope;
import com.example.cafeordersystem.global.event.ProcessedEventService;
import com.example.cafeordersystem.menu.dto.MenuEventPayload;
import com.example.cafeordersystem.menu.read.MenuReadMapper;
import com.example.cafeordersystem.menu.read.MenuReadRow;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사장 서버에서 발행한 메뉴 이벤트를 받아 구매 서버 menu_read를 갱신한다.
 *
 * 이벤트:
 * - menu.created
 * - menu.updated
 * - menu.deleted
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuEventConsumer {

    private final ObjectMapper objectMapper;
    private final ProcessedEventService processedEventService;
    private final MenuReadMapper menuReadMapper;

    @Transactional
    @KafkaListener(
            topics = {"menu.created", "menu.updated", "menu.deleted"},
            groupId = "customer-menu-read-service"
    )
    public void consume(String message) {
        try {
            EventEnvelope event = objectMapper.readValue(message, EventEnvelope.class);

            boolean firstProcess = processedEventService.tryMarkProcessed(
                    event.getEventId(),
                    event.getEventType()
            );

            if (!firstProcess) {
                log.info("중복 메뉴 이벤트 skip eventId={}", event.getEventId());
                return;
            }

            MenuEventPayload payload =
                    objectMapper.treeToValue(event.getPayload(), MenuEventPayload.class);

            if ("menu.deleted".equals(event.getEventType())) {
                menuReadMapper.markDeleted(payload.getMenuId());
                log.info("✅ menu_read 삭제 반영 menuId={}", payload.getMenuId());
                return;
            }

            MenuReadRow row = new MenuReadRow();
            row.setMenuId(payload.getMenuId());
            row.setMenuName(payload.getMenuName());
            row.setMenuPrice(payload.getMenuPrice());
            row.setMenuImage(payload.getMenuImage());

            menuReadMapper.upsertMenu(row);

            log.info("✅ menu_read upsert 완료 eventType={}, menuId={}",
                    event.getEventType(), payload.getMenuId());

        } catch (Exception e) {
            log.error("메뉴 이벤트 처리 실패 message={}", message, e);
            throw new RuntimeException(e);
        }
    }
}