package com.example.cafeordersystem.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReviewAnalysisItem {

    private Long reviewId;
    private Long orderId;
    private String reviewContent;
    private String createdAt;

    private String sentiment;
    private String summary;
    private String positivePoints;
    private String negativePoints;
    private String recommendedAction;
}