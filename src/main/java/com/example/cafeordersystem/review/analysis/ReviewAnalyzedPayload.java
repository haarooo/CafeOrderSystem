package com.example.cafeordersystem.review.analysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * review.analyzed 이벤트 payload.
 *
 * 구매 서버에서 리뷰 분석이 완료되거나 실패했을 때
 * 사장 서버의 review_read 분석 상태를 갱신하기 위해 발행한다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewAnalyzedPayload {

    private Long reviewId;

    private Long orderId;

    private String analysisStatus;

    private String analysisResultJson;

    private String analyzedAt;
}