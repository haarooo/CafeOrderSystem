package com.example.cafeordersystem.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 사장 서버가 메뉴 목록을 조회한 뒤 구매 서버로 돌려주는 Kafka 결과 이벤트.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuListQueryResultEvent {

    private String requestId;

    private Boolean success;

    private String message;

    private List<MenuItemDto> menus;
}