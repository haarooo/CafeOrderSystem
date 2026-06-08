package com.example.cafeordersystem.menu.kafka;

import com.example.cafeordersystem.menu.dto.MenuListQueryRequestEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * 구매 서버 → 사장 서버
 * 메뉴 목록 조회 요청 이벤트 발행.
 */
@Service
@RequiredArgsConstructor
public class MenuListQueryRequestProducer {

    private static final String TOPIC = "menu-list-query-request";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void send(MenuListQueryRequestEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(TOPIC, event.getRequestId(), json)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            System.out.println("✅ 메뉴 목록 조회 요청 발행 성공 requestId=" + event.getRequestId());
                        } else {
                            System.out.println("❌ 메뉴 목록 조회 요청 발행 실패");
                            ex.printStackTrace();
                        }
                    });

        } catch (Exception e) {
            throw new RuntimeException("메뉴 목록 조회 요청 이벤트 변환 실패", e);
        }
    }
}