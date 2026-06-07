package com.example.cafeordersystem.review.kafka;

import com.example.cafeordersystem.review.dto.ReviewAnalysisItem;
import com.example.cafeordersystem.review.dto.ReviewAnalysisRequestEvent;
import com.example.cafeordersystem.review.dto.ReviewAnalysisResultEvent;
import com.example.cafeordersystem.review.dto.ReviewRow;
import com.example.cafeordersystem.review.dto.RuntimeReviewAnalysisList;
import com.example.cafeordersystem.review.service.ReviewRuntimeAnalysisService;
import com.example.cafeordersystem.review.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewAnalysisRequestConsumer {

    private final ObjectMapper objectMapper;
    private final ReviewService reviewService;
    private final ReviewRuntimeAnalysisService reviewRuntimeAnalysisService;
    private final ReviewAnalysisResultProducer resultProducer;

    /**
     * 사장 서버가 대시보드/리뷰관리 화면에서 분석 요청을 보내면 구매 서버가 받는다.
     *
     * 이 Consumer는 리뷰 원문을 화면에 보내는 역할이 아니다.
     * 리뷰 원문은 review-list-query-request/result 흐름에서 먼저 처리한다.
     *
     * 여기서는 LLM 분석 결과만 만들어서 dashboard-review-analysis-result로 돌려준다.
     */
    @KafkaListener(
            topics = "dashboard-review-analysis-request",
            groupId = "customer-review-analysis-service"
    )
    public void consume(String message) {
        ReviewAnalysisRequestEvent request = null;

        try {
            log.info("📩 대시보드 리뷰 분석 요청 수신: {}", message);

            request = objectMapper.readValue(message, ReviewAnalysisRequestEvent.class);

            int page = Math.max(request.getPage(), 0);
            int size = Math.min(Math.max(request.getSize(), 1), 20);

            List<ReviewRow> reviews =
                    reviewService.getReviewRowsByPage(page, size);

            long totalCount = reviewService.countReviews();

            RuntimeReviewAnalysisList analysisList;

            try {
                analysisList = reviewRuntimeAnalysisService.analyzeReviews(reviews);
            } catch (Exception aiException) {
                log.error("❌ LLM 리뷰 분석 실패. 기본 분석값으로 응답합니다.", aiException);

                analysisList = RuntimeReviewAnalysisList.builder()
                        .analyses(List.of())
                        .build();
            }

            List<ReviewAnalysisItem> llmAnalyses =
                    analysisList == null || analysisList.getAnalyses() == null
                            ? List.of()
                            : analysisList.getAnalyses();

            /*
             * 핵심:
             * - 화면에는 이미 리뷰 원문이 먼저 떠 있다.
             * - 그래서 여기서는 reviewId 기준 분석 결과만 보낸다.
             * - LLM이 특정 reviewId 분석을 누락하면 기본값으로 채운다.
             */
            List<ReviewAnalysisItem> analyses = reviews.stream()
                    .map(review -> {
                        ReviewAnalysisItem analysis = llmAnalyses.stream()
                                .filter(item -> Objects.equals(
                                        item.getReviewId(),
                                        review.getReviewId()
                                ))
                                .findFirst()
                                .orElse(null);

                        return ReviewAnalysisItem.builder()
                                .reviewId(review.getReviewId())
                                .sentiment(analysis != null && analysis.getSentiment() != null
                                        ? analysis.getSentiment()
                                        : "NEUTRAL")
                                .summary(analysis != null && analysis.getSummary() != null
                                        ? analysis.getSummary()
                                        : "분석 결과를 생성하지 못했습니다.")
                                .operationInsight(analysis != null && analysis.getOperationInsight() != null
                                        ? analysis.getOperationInsight()
                                        : "리뷰 원문을 직접 확인해주세요.")
                                .build();
                    })
                    .toList();

            ReviewAnalysisResultEvent resultEvent =
                    ReviewAnalysisResultEvent.builder()
                            .requestId(request.getRequestId())
                            .page(page)
                            .size(size)
                            .totalCount(totalCount)
                            .analyses(analyses)
                            .message("리뷰 분석 완료")
                            .build();

            resultProducer.send(resultEvent);

        } catch (Exception e) {
            log.error("❌ 대시보드 리뷰 분석 요청 처리 실패", e);

            /*
             * 여기까지 왔다는 것은 request 파싱 자체가 실패했을 수도 있다.
             * requestId를 모르면 사장 서버 pending request를 완료시킬 수 없으므로
             * result 발행은 하지 않는다.
             */
        }
    }
}