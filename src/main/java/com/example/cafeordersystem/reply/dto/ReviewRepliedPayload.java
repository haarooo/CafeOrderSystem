package com.example.cafeordersystem.reply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 구매 서버가 답글 저장/수정/삭제 완료 후 Outbox로 발행하는 payload.
 *
 * 이벤트 타입:
 * - review.replied
 * - review.reply.updated
 * - review.reply.deleted
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRepliedPayload {

    private Long reviewId;

    private Long orderId;

    private String replyContent;

    private String replyStatus;

    private String repliedAt;

    private String replyUpdatedAt;
}