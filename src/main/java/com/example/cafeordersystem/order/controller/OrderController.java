package com.example.cafeordersystem.order.controller;

import com.example.cafeordersystem.order.dto.OrderCreateRequestDto;
import com.example.cafeordersystem.order.dto.OrderResponseDto;
import com.example.cafeordersystem.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestBody OrderCreateRequestDto request
    ) {
        try {
            return ResponseEntity.ok(orderService.createOrder(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("주문에 실패했습니다.");
        }
    }
}