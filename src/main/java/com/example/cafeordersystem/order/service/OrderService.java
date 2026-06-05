package com.example.cafeordersystem.order.service;

import com.example.cafeordersystem.order.dto.OrderCreateRequestDto;
import com.example.cafeordersystem.order.dto.OrderCreateRequestEvent;
import com.example.cafeordersystem.order.dto.OrderCreateResultEvent;
import com.example.cafeordersystem.order.dto.OrderResponseDto;
import com.example.cafeordersystem.order.kafka.OrderKafkaClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderKafkaClient orderKafkaClient;
    private final QrCodeService qrCodeService;

    @Value("${app.frontend.review-base-url}")
    private String reviewBaseUrl;

    public OrderResponseDto createOrder(OrderCreateRequestDto request) {
        validateRequest(request);

        String requestId = orderKafkaClient.createRequestId();

        OrderCreateRequestEvent event = OrderCreateRequestEvent.builder()
                .requestId(requestId)
                .items(request.getItems())
                .build();

        OrderCreateResultEvent result = orderKafkaClient.requestOrderCreate(event);

        // 핵심: QR에 들어갈 주소는 백엔드가 아니라 React 프론트 주소
        String reviewPageUrl = createReviewPageUrl(result.getOrderId());

        // QR 이미지는 구매 서버 uploads/qrcodes에 저장
        String qrUrl = qrCodeService.createQrImage(result.getOrderId(), reviewPageUrl);

        return OrderResponseDto.builder()
                .orderId(result.getOrderId())
                .orderPrice(result.getOrderPrice())
                .qrUrl(qrUrl)
                .reviewPageUrl(reviewPageUrl)
                .createdAt(result.getCreatedAt())
                .build();
    }

    private String createReviewPageUrl(Long orderId) {
        return reviewBaseUrl + "?orderId=" + orderId;
    }

    private void validateRequest(OrderCreateRequestDto request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("주문 메뉴는 최소 1개 이상이어야 합니다.");
        }
    }
}