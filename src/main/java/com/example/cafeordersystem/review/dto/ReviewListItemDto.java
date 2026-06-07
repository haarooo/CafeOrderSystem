package com.example.cafeordersystem.review.dto;

import lombok.Data;

@Data
public class ReviewListItemDto {

    private Long reviewId;
    private Long orderId;
    private String reviewContent;
    private String createdAt;
}
