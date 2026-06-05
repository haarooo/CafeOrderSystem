//package com.example.cafeordersystem.review.kafka;
//
//import com.example.cafeordersystem.review.dto.*;
//import com.example.cafeordersystem.review.service.ReviewRuntimeAnalysisService;
//import com.example.cafeordersystem.review.service.ReviewService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class ReviewAnalysisRequestConsumer {
//
//    private final ObjectMapper objectMapper;
//    private final ReviewService reviewService;
//    private final ReviewRuntimeAnalysisService reviewRuntimeAnalysisService;
//    private final ReviewAnalysisResultProducer resultProducer;
//
//    @KafkaListener(
//            topics = "dashboard-review-analysis-request",
//            groupId = "customer-review-analysis-service"
//    )
//    public void consume(String message) {
//        try {
//            log.info("대시보드 리뷰 분석 요청 수신: {}", message);
//
//            ReviewAnalysisRequestEvent request =
//                    objectMapper.readValue(message, ReviewAnalysisRequestEvent.class);
//
//            int page = Math.max(request.getPage(), 0);
//            int size = Math.min(Math.max(request.getSize(), 1), 20);
//
//            List<ReviewRow> reviews =
//                    reviewService.getReviewRowsByPage(page, size);
//
//            long totalCount = reviewService.countReviews();
//
//            RuntimeReviewAnalysisList analysisList =
//                    reviewRuntimeAnalysisService.analyzeReviews(reviews);
//
//            List<ReviewAnalysisItem> items = reviews.stream()
//                    .map(review -> {
//                        RuntimeReviewAnalysisItem analysis = analysisList.getAnalyses()
//                                .stream()
//                                .filter(item -> item.getReviewId().equals(review.getReviewId()))
//                                .findFirst()
//                                .orElse(null);
//
//                        return ReviewAnalysisItem.builder()
//                                .reviewId(review.getReviewId())
//                                .orderId(review.getOrderId())
//                                .reviewContent(review.getReviewContent())
//                                .createdAt(review.getCreatedAt())
//                                .sentiment(analysis != null ? analysis.getSentiment() : "NEUTRAL")
//                                .summary(analysis != null ? analysis.getSummary() : "분석 실패")
//                                .positivePoints(analysis != null ? analysis.getPositivePoints() : "")
//                                .negativePoints(analysis != null ? analysis.getNegativePoints() : "")
//                                .recommendedAction(analysis != null ? analysis.getRecommendedAction() : "리뷰 원문을 직접 확인해주세요.")
//                                .build();
//                    })
//                    .toList();
//
//            ReviewAnalysisResultEvent resultEvent =
//                    ReviewAnalysisResultEvent.builder()
//                            .requestId(request.getRequestId())
//                            .page(page)
//                            .size(size)
//                            .totalCount(totalCount)
//                            .reviews(items)
//                            .build();
//
//            resultProducer.send(resultEvent);
//
//        } catch (Exception e) {
//            log.error("대시보드 리뷰 분석 요청 처리 실패", e);
//        }
//    }
//}