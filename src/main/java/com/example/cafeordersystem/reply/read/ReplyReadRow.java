package com.example.cafeordersystem.reply.read;

import lombok.Data;

@Data
public class ReplyReadRow {

    private Long customerReviewId;

    private Long orderId;

    private String replyContent;

    private Boolean hasReply;

    private String repliedAt;

    private String updatedAt;
}