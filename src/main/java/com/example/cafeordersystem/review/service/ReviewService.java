package com.example.cafeordersystem.review.service;

import com.example.cafeordersystem.global.outbox.OutboxService;
import com.example.cafeordersystem.order.read.ReviewableOrderReadMapper;
import com.example.cafeordersystem.review.dto.ReviewCreateRequestDto;
import com.example.cafeordersystem.review.dto.ReviewCreatedPayload;
import com.example.cafeordersystem.review.dto.ReviewResponseDto;
import com.example.cafeordersystem.review.dto.ReviewRow;
import com.example.cafeordersystem.review.mapper.ReviewMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 고객 리뷰 서비스.
 *
 * 핵심 변경:
 * - 리뷰 저장과 review.created outbox 저장을 같은 트랜잭션 안에서 처리한다.
 * - Kafka 직접 발행은 하지 않는다.
 * - OutboxRelay가 나중에 outbox NEW 이벤트를 Kafka로 발행한다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private static final String REVIEW_CREATED_TOPIC = "review.created";
    private static final String REVIEW_CREATED_EVENT_TYPE = "review.created";
    private static final String REVIEW_AGGREGATE_TYPE = "REVIEW";

    private final ReviewMapper reviewMapper;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;
    private final ReviewableOrderReadMapper reviewableOrderReadMapper;



    public ReviewResponseDto createReview(ReviewCreateRequestDto request) {
        validateCreateReviewRequest(request);

        if (reviewMapper.existsByOrderId(request.getOrderId()) > 0) {
            throw new RuntimeException("이미 해당 주문에 리뷰가 존재합니다.");
        }

        // 1. 리뷰 저장
        reviewMapper.insertReview(request);

        // 2. 저장된 리뷰 재조회
        ReviewRow savedReview = reviewMapper.findByOrderId(request.getOrderId());

        if (savedReview == null || savedReview.getReviewId() == null) {
            throw new RuntimeException("저장된 리뷰를 조회하지 못했습니다.");
        }

        // 3. review.created payload 생성
        ReviewCreatedPayload payload = ReviewCreatedPayload.builder()
                .reviewId(savedReview.getReviewId())
                .orderId(savedReview.getOrderId())
                .reviewContent(savedReview.getReviewContent())
                .createdAt(savedReview.getCreatedAt())
                .build();

        JsonNode payloadNode = objectMapper.valueToTree(payload);

        // 4. 같은 트랜잭션 안에서 outbox 저장
        // aggregateId를 reviewId로 사용한다.
        // OutboxRelay는 kafkaKey=reviewId로 Kafka에 발행한다.
        outboxService.saveEvent(
                REVIEW_CREATED_TOPIC,
                REVIEW_CREATED_EVENT_TYPE,
                REVIEW_AGGREGATE_TYPE,
                String.valueOf(savedReview.getReviewId()),
                payloadNode
        );

        reviewableOrderReadMapper.markReviewWritten(savedReview.getOrderId());

        return ReviewResponseDto.createSuccess(
                savedReview,
                "리뷰가 성공적으로 작성되었습니다."
        );
    }

    @Transactional(readOnly = true)
    public ReviewResponseDto getReview(Long reviewId) {
        ReviewRow review = reviewMapper.findByReviewId(reviewId);

        if (review == null) {
            throw new RuntimeException("리뷰를 찾을 수 없습니다: " + reviewId);
        }

        return ReviewResponseDto.from(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewRow> getReviewRowsByPage(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 20);
        int offset = safePage * safeSize;

        return reviewMapper.findReviewsByPaging(offset, safeSize);
    }

    @Transactional(readOnly = true)
    public long countReviews() {
        return reviewMapper.countReviews();
    }



    private void validateCreateReviewRequest(ReviewCreateRequestDto request) {
        if (request.getOrderId() == null) {
            throw new RuntimeException("주문 ID는 필수입니다.");
        }

        if (request.getReviewContent() == null || request.getReviewContent().isBlank()) {
            throw new RuntimeException("리뷰 내용은 필수입니다.");
        }
    }
}