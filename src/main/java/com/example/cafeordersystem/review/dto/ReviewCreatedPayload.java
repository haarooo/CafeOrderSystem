package com.example.cafeordersystem.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * review.created 이벤트의 payload.
 *
 * 이 payload는 구매 서버에서 리뷰가 작성됐다는 사실을
 * 사장 서버에 전달하기 위해 사용한다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreatedPayload {

    private Long reviewId;

    private Long orderId;

    private String reviewContent;

    private String createdAt;
}