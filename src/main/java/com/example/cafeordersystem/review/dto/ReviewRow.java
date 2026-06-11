package com.example.cafeordersystem.review.dto;

import lombok.Data;

@Data
public class ReviewRow {

    private Long reviewId;

    private Long orderId;

    private String reviewContent;

    private String analysisStatus;

    private String analysisResultJson;

    private String analyzedAt;

    private String replyContent;

    private String replyStatus;

    private String repliedAt;

    private String replyUpdatedAt;

    private String createdAt;

    private String updatedAt;
}