package com.example.cafeordersystem.order.kafka;

import com.example.cafeordersystem.order.dto.OrderCreateResultEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCreateResultConsumer {

    private final ObjectMapper objectMapper;
    private final OrderKafkaClient orderKafkaClient;

    @KafkaListener(
            topics = "order-create-result",
            groupId = "customer-order-result-service"
    )
    public void consume(String message) {
        try {
            log.info("주문 생성 결과 수신: {}", message);

            OrderCreateResultEvent result =
                    objectMapper.readValue(message, OrderCreateResultEvent.class);

            orderKafkaClient.complete(result);

        } catch (Exception e) {
            log.error("주문 생성 결과 처리 실패", e);
        }
    }
}