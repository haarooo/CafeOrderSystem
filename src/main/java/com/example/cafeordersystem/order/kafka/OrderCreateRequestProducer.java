package com.example.cafeordersystem.order.kafka;

import com.example.cafeordersystem.order.dto.OrderCreateRequestEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCreateRequestProducer {

    private static final String TOPIC = "order-create-request";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void send(OrderCreateRequestEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(TOPIC, event.getRequestId(), json)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            System.out.println("✅ 주문 생성 요청 발행 성공 requestId=" + event.getRequestId());
                        } else {
                            System.out.println("❌ 주문 생성 요청 발행 실패");
                            ex.printStackTrace();
                        }
                    });

        } catch (Exception e) {
            throw new RuntimeException("주문 생성 요청 이벤트 변환 실패", e);
        }
    }
}