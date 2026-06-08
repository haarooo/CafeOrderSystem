package com.example.cafeordersystem.order.read;

import lombok.Data;

@Data
public class ReviewableOrderReadRow {

    private Long orderId;

    private Integer orderPrice;

    private String createdAt;

    private Boolean reviewWritten;
}