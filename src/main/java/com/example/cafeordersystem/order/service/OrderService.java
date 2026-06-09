package com.example.cafeordersystem.order.service;

import com.example.cafeordersystem.order.dto.OrderCreateRequestDto;
import com.example.cafeordersystem.order.dto.OrderCreateRequestEvent;
import com.example.cafeordersystem.order.dto.OrderCreateResultEvent;
import com.example.cafeordersystem.order.dto.OrderResponseDto;
import com.example.cafeordersystem.order.kafka.OrderKafkaClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 구매 서버 주문 서비스.
 *
 * 주문 자체는 Kafka request/reply로 사장 서버에 요청한다.
 * 사장 서버는 주문 저장 후 orderId/orderPrice만 반환한다.
 *
 * QR 생성은 구매 서버가 담당한다.
 * 단, QR 이미지는 저장하지 않고 Base64 data URL로 프론트에 바로 내려준다.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderKafkaClient orderKafkaClient;
    private final QrCodeService qrCodeService;

    /**
     * QR 안에 들어갈 리뷰 작성 페이지 주소.
     *
     * 로컬:
     * http://localhost:5173/review/write
     *
     * 배포:
     * https://구매서버도메인/review/write
     */
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

        /*
         * QR을 찍었을 때 이동할 구매 프론트 리뷰 작성 URL.
         */
        String reviewPageUrl = createReviewPageUrl(result.getOrderId());

        /*
         * QR 이미지는 저장하지 않고 Base64 data URL로 생성한다.
         * 프론트에서는 <img src={qrUrl}> 로 바로 표시된다.
         */
        String qrUrl = qrCodeService.createQrImage(
                result.getOrderId(),
                reviewPageUrl
        );

        return OrderResponseDto.builder()
                .orderId(result.getOrderId())
                .orderPrice(result.getOrderPrice())
                .qrUrl(qrUrl)
                .reviewPageUrl(reviewPageUrl)
                .createdAt(result.getCreatedAt())
                .build();
    }

    private String createReviewPageUrl(Long orderId) {
        return reviewBaseUrl.replaceAll("/+$", "") + "?orderId=" + orderId;
    }

    private void validateRequest(OrderCreateRequestDto request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("주문 메뉴는 최소 1개 이상이어야 합니다.");
        }
    }
}