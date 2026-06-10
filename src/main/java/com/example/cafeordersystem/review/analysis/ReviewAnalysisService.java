package com.example.cafeordersystem.review.analysis;

import com.example.cafeordersystem.global.outbox.OutboxService;
import com.example.cafeordersystem.review.dto.ReviewRow;
import com.example.cafeordersystem.review.mapper.ReviewMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 구매 서버 리뷰 분석 상태 변경과 review.analyzed 이벤트 발행을 담당한다.
 *
 * 핵심:
 * - LLM 호출 중에는 DB 트랜잭션을 잡지 않는다.
 * - PENDING/FAILED → PROCESSING 선점은 짧은 트랜잭션으로 끝낸다.
 * - 분석 성공/실패 저장과 review.analyzed outbox 저장은 같은 트랜잭션으로 묶는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewAnalysisService {

    private static final int BATCH_SIZE = 10;

    private static final int FAILED_RETRY_MINUTES = 10;
    private static final int PROCESSING_TIMEOUT_MINUTES = 5;

    private static final String REVIEW_ANALYZED_TOPIC = "review.analyzed";
    private static final String REVIEW_ANALYZED_EVENT_TYPE = "review.analyzed";
    private static final String REVIEW_AGGREGATE_TYPE = "REVIEW";

    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";

    private final ReviewMapper reviewMapper;
    private final ReviewAnalysisClient reviewAnalysisClient;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;

    /**
     * PROCESSING 상태로 오래 고착된 리뷰를 PENDING으로 복구한다.
     *
     * 예:
     * - LLM 호출 중 서버 재시작
     * - devtools 자동 재시작
     * - 프로세스 강제 종료
     */
    @Transactional
    public int recoverStaleProcessing() {
        LocalDateTime processingTimeoutDeadline =
                LocalDateTime.now().minusMinutes(PROCESSING_TIMEOUT_MINUTES);

        return reviewMapper.resetStaleProcessing(processingTimeoutDeadline);
    }

    /**
     * 분석 대상 리뷰를 조회하고 PROCESSING으로 선점한다.
     *
     * 대상:
     * - PENDING
     * - FAILED 중 updated_at 기준 10분 이상 지난 것
     */
    @Transactional
    public List<ReviewAnalysisTarget> claimAnalysisTargets() {
        LocalDateTime failedRetryDeadline =
                LocalDateTime.now().minusMinutes(FAILED_RETRY_MINUTES);

        List<ReviewRow> targets = reviewMapper.findAnalysisTargets(
                failedRetryDeadline,
                BATCH_SIZE
        );

        if (targets.isEmpty()) {
            return List.of();
        }

        for (ReviewRow target : targets) {
            reviewMapper.markAnalysisProcessing(target.getReviewId());
        }

        return targets.stream()
                .map(review -> new ReviewAnalysisTarget(
                        review.getReviewId(),
                        review.getOrderId(),
                        review.getReviewContent()
                ))
                .toList();
    }

    /**
     * 외부 LLM API 호출.
     *
     * 일부러 @Transactional을 붙이지 않는다.
     * 외부 API 호출 중 DB 트랜잭션을 오래 잡지 않기 위해서다.
     */
    public String analyzeReviewContent(String reviewContent) {
        return reviewAnalysisClient.analyze(reviewContent);
    }

    /**
     * 분석 성공 처리.
     *
     * review 테이블 업데이트와 review.analyzed outbox 저장을 같은 트랜잭션으로 처리한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeAnalysis(
            Long reviewId,
            String analysisResultJson
    ) {
        reviewMapper.markAnalysisCompleted(
                reviewId,
                analysisResultJson
        );

        ReviewRow updatedReview = reviewMapper.findByReviewId(reviewId);

        if (updatedReview == null) {
            throw new RuntimeException("분석 완료 후 리뷰를 찾을 수 없습니다. reviewId=" + reviewId);
        }

        saveReviewAnalyzedEvent(
                updatedReview.getReviewId(),
                updatedReview.getOrderId(),
                STATUS_COMPLETED,
                updatedReview.getAnalysisResultJson(),
                updatedReview.getAnalyzedAt()
        );

        log.info("✅ 리뷰 분석 완료 저장 및 이벤트 생성 reviewId={}, status={}",
                reviewId,
                STATUS_COMPLETED
        );
    }

    /**
     * 분석 실패 처리.
     *
     * FAILED 상태로 저장하고 review.analyzed 이벤트를 발행한다.
     * 사장 서버는 이 이벤트를 받아 review_read 상태를 FAILED로 갱신한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failAnalysis(Long reviewId) {
        reviewMapper.markAnalysisFailed(reviewId);

        ReviewRow failedReview = reviewMapper.findByReviewId(reviewId);

        if (failedReview == null) {
            throw new RuntimeException("분석 실패 후 리뷰를 찾을 수 없습니다. reviewId=" + reviewId);
        }

        saveReviewAnalyzedEvent(
                failedReview.getReviewId(),
                failedReview.getOrderId(),
                STATUS_FAILED,
                null,
                null
        );

        log.warn("⚠️ 리뷰 분석 실패 저장 및 이벤트 생성 reviewId={}, status={}",
                reviewId,
                STATUS_FAILED
        );
    }

    private void saveReviewAnalyzedEvent(
            Long reviewId,
            Long orderId,
            String analysisStatus,
            String analysisResultJson,
            String analyzedAt
    ) {
        ReviewAnalyzedPayload payload = ReviewAnalyzedPayload.builder()
                .reviewId(reviewId)
                .orderId(orderId)
                .analysisStatus(analysisStatus)
                .analysisResultJson(analysisResultJson)
                .analyzedAt(analyzedAt)
                .build();

        JsonNode payloadNode = objectMapper.valueToTree(payload);

        outboxService.saveEvent(
                REVIEW_ANALYZED_TOPIC,
                REVIEW_ANALYZED_EVENT_TYPE,
                REVIEW_AGGREGATE_TYPE,
                String.valueOf(reviewId),
                payloadNode
        );
    }
}