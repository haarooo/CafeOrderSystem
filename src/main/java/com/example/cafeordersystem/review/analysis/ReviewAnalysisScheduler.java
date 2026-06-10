package com.example.cafeordersystem.review.analysis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 구매 서버 리뷰 분석 스케줄러.
 *
 * review 테이블에서 PENDING/FAILED 상태 리뷰를 가져와
 * PROCESSING → COMPLETED 또는 FAILED로 변경한다.
 *
 * 분석 결과는 review.analyzed 이벤트로 사장 서버에 전달된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewAnalysisScheduler {

    private final ReviewAnalysisService reviewAnalysisService;

    @Scheduled(fixedDelay = 5000)
    public void analyzeReviews() {
        int recoveredCount = reviewAnalysisService.recoverStaleProcessing();

        if (recoveredCount > 0) {
            log.warn("PROCESSING 상태 고착 리뷰 복구 완료 count={}", recoveredCount);
        }

        List<ReviewAnalysisTarget> targets =
                reviewAnalysisService.claimAnalysisTargets();

        if (targets.isEmpty()) {
            return;
        }

        log.info("리뷰 분석 대상 {}건 조회", targets.size());

        for (ReviewAnalysisTarget target : targets) {
            try {
                log.info("리뷰 분석 시작 reviewId={}, orderId={}",
                        target.reviewId(),
                        target.orderId()
                );

                String analysisResultJson =
                        reviewAnalysisService.analyzeReviewContent(target.reviewContent());

                reviewAnalysisService.completeAnalysis(
                        target.reviewId(),
                        analysisResultJson
                );

            } catch (Exception e) {
                log.error("❌ 리뷰 분석 실패 reviewId={}", target.reviewId(), e);

                reviewAnalysisService.failAnalysis(target.reviewId());
            }
        }
    }
}