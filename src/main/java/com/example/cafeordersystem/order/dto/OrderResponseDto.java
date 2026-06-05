package com.example.cafeordersystem.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderResponseDto {

    private Long orderId;
    private Integer orderPrice;
    private String qrUrl;
    private String reviewPageUrl;
    private String createdAt;
}