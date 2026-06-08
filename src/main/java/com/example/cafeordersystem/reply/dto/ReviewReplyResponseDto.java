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
}