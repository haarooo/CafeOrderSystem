package com.example.cafeordersystem.menu.kafka;

import com.example.cafeordersystem.menu.dto.OwnerMenuDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * 사장 서버 메뉴 원장을 전체 조회하는 클라이언트.
 *
 * POS 조회 요청마다 호출하는 용도가 아니다.
 * 구매 서버의 menu_read를 시작 시점과 주기적으로 복구하는 용도다.
 */
@Component
public class OwnerMenuClient {

    private final RestClient restClient;

    public OwnerMenuClient(
            @Value("${owner.server.base-url}") String ownerServerBaseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(ownerServerBaseUrl)
                .build();
    }

    public List<OwnerMenuDto> getAllMenus() {
        List<OwnerMenuDto> menus = restClient.get()
                .uri("/api/menu")
                .retrieve()
                .body(new ParameterizedTypeReference<List<OwnerMenuDto>>() {
                });

        return menus == null ? List.of() : menus;
    }
}
