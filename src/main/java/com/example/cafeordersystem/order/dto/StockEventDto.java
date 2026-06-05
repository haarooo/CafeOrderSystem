package com.example.cafeordersystem.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockEventDto {

    private Long ingredientId; // 자재 ID (예: 우유 ID는 4)
    private int amount;
}
