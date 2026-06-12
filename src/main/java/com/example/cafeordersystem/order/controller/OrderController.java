package com.example.cafeordersystem.order.controller;

import com.example.cafeordersystem.order.dto.OrderCreateRequestDto;
import com.example.cafeordersystem.order.dto.OrderResponseDto;
import com.example.cafeordersystem.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
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
            OrderResponseDto response = orderService.createOrder(request);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("주문 API 실패 request={}, error={}", request, e.getMessage(), e);

            return ResponseEntity.badRequest()
                    .body(e.getMessage() != null ? e.getMessage() : "주문에 실패했습니다.");
        }
    }
}