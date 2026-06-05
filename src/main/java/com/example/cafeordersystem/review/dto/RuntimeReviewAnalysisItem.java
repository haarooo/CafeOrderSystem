package com.example.cafeordersystem.review.dto;

import lombok.Data;

@Data
public class RuntimeReviewAnalysisItem {

    private Long reviewId;
    private String sentiment;
    private String summary;
    private String positivePoints;
    private String negativePoints;
    private String recommendedAction;
}