package com.example.cafeordersystem.review.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewResponseDto {

    private Long reviewId;
    private Long orderId;
    private String reviewContent;
    private String message;
    private String createdAt;
    private String updatedAt;

    public static ReviewResponseDto createSuccess(ReviewRow review, String message) {
        return ReviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .orderId(review.getOrderId())
                .reviewContent(review.getReviewContent())
                .message(message)
                .createdAt(review.getCreatedAt())
                .build();
    }

    public static ReviewResponseDto from(ReviewRow review) {
        return ReviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .orderId(review.getOrderId())
                .reviewContent(review.getReviewContent())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}