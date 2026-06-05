package com.example.cafeordersystem.order.kafka;

import com.example.cafeordersystem.order.dto.OrderCreateRequestEvent;
import com.example.cafeordersystem.order.dto.OrderCreateResultEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderKafkaClient {

    private final OrderCreateRequestProducer orderCreateRequestProducer;

    private final Map<String, CompletableFuture<OrderCreateResultEvent>> pendingRequests =
            new ConcurrentHashMap<>();

    public OrderCreateResultEvent requestOrderCreate(OrderCreateRequestEvent event) {
        try {
            CompletableFuture<OrderCreateResultEvent> future = new CompletableFuture<>();
            pendingRequests.put(event.getRequestId(), future);

            orderCreateRequestProducer.send(event);

            return future.get(15, TimeUnit.SECONDS);

        } catch (Exception e) {
            throw new RuntimeException("주문 생성 결과 수신 실패", e);
        }
    }

    public void complete(OrderCreateResultEvent resultEvent) {
        CompletableFuture<OrderCreateResultEvent> future =
                pendingRequests.remove(resultEvent.getRequestId());

        if (future != null) {
            future.complete(resultEvent);
        }
    }

    public String createRequestId() {
        return UUID.randomUUID().toString();
    }
}