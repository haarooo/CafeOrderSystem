package com.example.cafeordersystem.review.analysis;

public record ReviewAnalysisTarget(
        Long reviewId,
        Long orderId,
        String reviewContent
) {
}