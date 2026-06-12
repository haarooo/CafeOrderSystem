package com.example.cafeordersystem.order.kafka;

import com.example.cafeordersystem.order.dto.OrderCreateRequestEvent;
import com.example.cafeordersystem.order.dto.OrderCreateResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderKafkaClient {

    private final OrderCreateRequestProducer orderCreateRequestProducer;

    private final Map<String, CompletableFuture<OrderCreateResultEvent>> pendingRequests =
            new ConcurrentHashMap<>();

    public OrderCreateResultEvent requestOrderCreate(OrderCreateRequestEvent event) {
        CompletableFuture<OrderCreateResultEvent> future = new CompletableFuture<>();
        pendingRequests.put(event.getRequestId(), future);

        try {
            log.info("주문 생성 Kafka 요청 시작 requestId={}, items={}",
                    event.getRequestId(),
                    event.getItems()
            );

            orderCreateRequestProducer.send(event);

            OrderCreateResultEvent result = future.get(15, TimeUnit.SECONDS);

            log.info("주문 생성 Kafka 결과 수신 완료 requestId={}, success={}, orderId={}, message={}",
                    result.getRequestId(),
                    result.getSuccess(),
                    result.getOrderId(),
                    result.getMessage()
            );

            return result;

        } catch (Exception e) {
            log.error("주문 생성 결과 수신 실패 requestId={}, error={}",
                    event.getRequestId(),
                    e.getMessage(),
                    e
            );

            Throwable cause = e.getCause();

            if (cause != null && cause.getMessage() != null) {
                throw new RuntimeException(cause.getMessage(), e);
            }

            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : "주문 생성 결과 수신 실패", e);

        } finally {
            pendingRequests.remove(event.getRequestId());
        }
    }

    public void complete(OrderCreateResultEvent resultEvent) {
        CompletableFuture<OrderCreateResultEvent> future =
                pendingRequests.remove(resultEvent.getRequestId());

        if (future == null) {
            log.warn("대기 중인 주문 요청을 찾지 못했습니다 requestId={}, result={}",
                    resultEvent.getRequestId(),
                    resultEvent
            );
            return;
        }

        if (Boolean.TRUE.equals(resultEvent.getSuccess())) {
            future.complete(resultEvent);
            return;
        }

        String message = resultEvent.getMessage() != null
                ? resultEvent.getMessage()
                : "주문 처리에 실패했습니다.";

        future.completeExceptionally(new RuntimeException(message));
    }

    public String createRequestId() {
        return UUID.randomUUID().toString();
    }
}