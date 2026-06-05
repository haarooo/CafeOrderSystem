package com.example.cafeordersystem.order.service;


import com.example.cafeordersystem.order.dto.StockEventDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CustomerOrderService {

    // 🚀 스프링이 제공하는 카프카 전송 해결사 리포지토리 역할
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper; // Object -> JSON 변환기

    public CustomerOrderService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<?> processCustomerOrder() {
        // [비즈니스 로직] 고객이 결제를 완료하면...

        try {
            // 1. 소모 데이터 생성 (우유 ID 4번, 2개 소모 발동)
            StockEventDto event = new StockEventDto(4L, -2);

            // 2. 자바 객체를 "{"ingredientId":4, "amount":-2}" 형태의 JSON 문자열로 변환
            String jsonMessage = objectMapper.writeValueAsString(event);

            // 3. 민서님이 열어둔 "stock-consume-events" 방(Topic)으로 던지기!
            System.out.println("🚀 [고객 서버] 민서 컴퓨터 카프카로 실시간 소모 신호 발송!");
            kafkaTemplate.send("stock-consume-events", jsonMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}