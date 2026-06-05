package com.example.cafeordersystem.reply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReviewReplyQueryResultEvent {

    private String requestId;
    private Long customerReviewId;
    private Long orderId;
    private Boolean hasReply;
    private String replyContent;
    private String repliedAt;
    private String message;
}