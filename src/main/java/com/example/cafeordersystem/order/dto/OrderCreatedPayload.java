package com.example.cafeordersystem.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사장 서버에서 발행할 order.created 이벤트 payload.
 *
 * 구매 서버는 이 이벤트를 받아 reviewable_order_read에 저장한다.
 * 이후 리뷰 작성 시 사장 서버에 묻지 않고 구매 서버 DB만 보고 주문 검증이 가능하다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedPayload {

    private Long orderId;

    private Integer orderPrice;

    private String createdAt;
}