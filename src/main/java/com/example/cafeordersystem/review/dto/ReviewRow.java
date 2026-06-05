package com.example.cafeordersystem.review.dto;

import lombok.Data;

@Data
public class ReviewRow {

    private Long reviewId;
    private Long orderId;
    private String reviewContent;
    private String createdAt;
    private String updatedAt;
}