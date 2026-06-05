package com.example.cafeordersystem.review.kafka;

import com.example.cafeordersystem.review.dto.ReviewAnalysisResultEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewAnalysisResultProducer {

    private static final String TOPIC = "dashboard-review-analysis-result";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void send(ReviewAnalysisResultEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(TOPIC, event.getRequestId(), json)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            System.out.println("✅ 대시보드 리뷰 분석 결과 발행 성공 requestId=" + event.getRequestId());
                        } else {
                            System.out.println("❌ 대시보드 리뷰 분석 결과 발행 실패");
                            ex.printStackTrace();
                        }
                    });

        } catch (Exception e) {
            throw new RuntimeException("대시보드 리뷰 분석 결과 이벤트 변환 실패", e);
        }
    }
}