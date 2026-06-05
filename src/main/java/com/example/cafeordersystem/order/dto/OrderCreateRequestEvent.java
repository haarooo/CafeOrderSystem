package com.example.cafeordersystem.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderCreateRequestEvent {

    private String requestId;
    private List<OrderItemRequestDto> items;
}