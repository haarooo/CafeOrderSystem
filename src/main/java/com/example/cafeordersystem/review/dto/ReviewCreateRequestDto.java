package com.example.cafeordersystem.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReviewCreateRequestDto {

    private Long orderId;
    private String reviewContent;
}
