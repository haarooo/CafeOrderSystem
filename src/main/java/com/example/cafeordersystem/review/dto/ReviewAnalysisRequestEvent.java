package com.example.cafeordersystem.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReviewAnalysisRequestEvent {

    private String requestId;
    private int page;
    private int size;
}