package com.example.cafeordersystem.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderCreateResultEvent {

    private String requestId;
    private Long orderId;
    private Integer orderPrice;
    private String createdAt;
}