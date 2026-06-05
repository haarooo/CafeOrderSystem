package com.example.cafeordersystem.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReviewAnalysisResultEvent {

    private String requestId;
    private int page;
    private int size;
    private long totalCount;
    private List<ReviewAnalysisItem> reviews;
}