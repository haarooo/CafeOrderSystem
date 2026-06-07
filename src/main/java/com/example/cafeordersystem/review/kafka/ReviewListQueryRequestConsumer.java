package com.example.cafeordersystem.review.kafka;

import com.example.cafeordersystem.review.dto.ReviewListItemDto;
import com.example.cafeordersystem.review.dto.ReviewListQueryRequestEvent;
import com.example.cafeordersystem.review.dto.ReviewListQueryResultEvent;
import com.example.cafeordersystem.review.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewListQueryRequestConsumer {

    private final ObjectMapper objectMapper;
    private final ReviewService reviewService;
    private final ReviewListQueryResultProducer resultProducer;

    @KafkaListener(
            topics = "review-list-query-request",
            groupId = "customer-review-list-service"
    )
    public void consume(String message) {
        try {
            log.info("📩 리뷰 목록 조회 요청 수신");

            ReviewListQueryRequestEvent request =
                    objectMapper.readValue(message, ReviewListQueryRequestEvent.class);

            int page = Math.max(request.getPage(), 0);

            List<ReviewListItemDto> reviews = reviewService.getOwnerReviewList(page);
            long totalCount = reviewService.countReviews();

            ReviewListQueryResultEvent result = ReviewListQueryResultEvent.builder()
                    .requestId(request.getRequestId())
                    .reviews(reviews)
                    .page(page)
                    .size(10)
                    .totalCount(totalCount)
                    .message("리뷰 목록 조회 성공")
                    .build();

            resultProducer.send(result);

        } catch (Exception e) {
            log.error("리뷰 목록 조회 요청 처리 실패", e);
        }
    }
}
