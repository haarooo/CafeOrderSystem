package com.example.cafeordersystem.review.dto;

import lombok.Data;

import java.util.List;

@Data
public class RuntimeReviewAnalysisList {

    private List<RuntimeReviewAnalysisItem> analyses;
}