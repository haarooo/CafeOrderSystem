package com.example.cafeordersystem.reply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사장 서버에서 발행할 reply.created / reply.updated / reply.deleted 이벤트 payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyEventPayload {

    private Long customerReviewId;

    private Long orderId;

    private String replyContent;

    private String repliedAt;
}