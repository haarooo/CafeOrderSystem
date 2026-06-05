package com.example.cafeordersystem.reply.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewReplyResponseDto {

    private Long customerReviewId;
    private Long orderId;
    private Boolean hasReply;
    private String replyContent;
    private String repliedAt;
    private String message;

    public static ReviewReplyResponseDto from(
            ReviewReplyQueryResultEvent event
    ) {
        return ReviewReplyResponseDto.builder()
                .customerReviewId(event.getCustomerReviewId())
                .orderId(event.getOrderId())
                .hasReply(event.getHasReply())
                .replyContent(event.getReplyContent())
                .repliedAt(event.getRepliedAt())
                .message(event.getMessage())
                .build();
    }
}