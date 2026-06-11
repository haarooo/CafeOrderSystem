package com.example.cafeordersystem.review.reply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 구매 서버가 사장 서버에 돌려주는 답글 명령 처리 결과.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReplyCommandResultEvent {

    private String requestId;

    private String commandType;

    private Long reviewId;

    private Long orderId;

    private String replyContent;

    private String replyStatus;

    private String repliedAt;

    private String replyUpdatedAt;

    private Boolean success;

    private String message;
}