package com.example.cafeordersystem.review.kafka;

import com.example.cafeordersystem.review.dto.ReviewListQueryResultEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewListQueryResultProducer {

    private static final String TOPIC = "review-list-query-result";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void send(ReviewListQueryResultEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(TOPIC, event.getRequestId(), json)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            System.out.println("✅ 리뷰 목록 조회 결과 발행 성공 requestId=" + event.getRequestId());
                        } else {
                            System.out.println("❌ 리뷰 목록 조회 결과 발행 실패");
                            ex.printStackTrace();
                        }
                    });

        } catch (Exception e) {
            throw new RuntimeException("리뷰 목록 조회 결과 이벤트 변환 실패", e);
        }
    }
}
