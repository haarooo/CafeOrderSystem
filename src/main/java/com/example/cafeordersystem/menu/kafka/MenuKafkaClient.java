package com.example.cafeordersystem.menu.kafka;

import com.example.cafeordersystem.menu.dto.MenuListQueryRequestEvent;
import com.example.cafeordersystem.menu.dto.MenuListQueryResultEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 구매 서버에서 사장 서버 메뉴 목록을 Kafka request/reply 방식으로 조회하는 클라이언트.
 */
@Service
@RequiredArgsConstructor
public class MenuKafkaClient {

    private final MenuListQueryRequestProducer requestProducer;

    private final Map<String, CompletableFuture<MenuListQueryResultEvent>> pendingRequests =
            new ConcurrentHashMap<>();

    public MenuListQueryResultEvent requestMenus() {
        String requestId = UUID.randomUUID().toString();

        CompletableFuture<MenuListQueryResultEvent> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);

        try {
            MenuListQueryRequestEvent event = MenuListQueryRequestEvent.builder()
                    .requestId(requestId)
                    .build();

            requestProducer.send(event);

            return future.get(10, TimeUnit.SECONDS);

        } catch (Exception e) {
            throw new RuntimeException("메뉴 목록 조회 결과 수신 실패", e);

        } finally {
            pendingRequests.remove(requestId);
        }
    }

    public void complete(MenuListQueryResultEvent resultEvent) {
        CompletableFuture<MenuListQueryResultEvent> future =
                pendingRequests.get(resultEvent.getRequestId());

        if (future == null) {
            return;
        }

        if (Boolean.TRUE.equals(resultEvent.getSuccess())) {
            future.complete(resultEvent);
        } else {
            future.completeExceptionally(
                    new RuntimeException(
                            resultEvent.getMessage() != null
                                    ? resultEvent.getMessage()
                                    : "메뉴 목록 조회에 실패했습니다."
                    )
            );
        }
    }
}