package com.example.cafeordersystem.review.reply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사장 서버가 구매 서버에 보내는 답글 명령 요청 이벤트.
 *
 * commandType:
 * - CREATE
 * - UPDATE
 * - DELETE
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReplyCommandRequestEvent {

    private String requestId;

    private String commandType;

    private Long reviewId;

    private Long orderId;

    private String replyContent;

    private String requestedAt;
}