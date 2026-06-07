package com.example.cafeordersystem.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReviewListQueryRequestEvent {

    private String requestId;
    private int page;
}
