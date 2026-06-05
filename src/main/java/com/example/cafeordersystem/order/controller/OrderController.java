package com.example.cafeordersystem.order.controller;

import com.example.cafeordersystem.order.service.CustomerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CustomerOrderService customerOrderService;

    @GetMapping
    public ResponseEntity<?> getKafka(){
       return customerOrderService.processCustomerOrder();
    }


}