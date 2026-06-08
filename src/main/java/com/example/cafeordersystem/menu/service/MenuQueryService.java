package com.example.cafeordersystem.menu.service;

import com.example.cafeordersystem.menu.dto.MenuResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 구매 서버가 사장 서버의 메뉴 API를 호출하는 서비스.
 *
 * React POS는 구매 서버만 바라보고,
 * 구매 서버가 내부적으로 사장 서버 /api/menu를 호출한다.
 */
@Service
public class MenuQueryService {

    private final RestClient restClient;

    public MenuQueryService(
            @Value("${owner.server.base-url}") String ownerServerBaseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(ownerServerBaseUrl)
                .build();
    }

    public List<MenuResponseDto> getMenus() {
        try {
            List<MenuResponseDto> ownerMenus = restClient.get()
                    .uri("/api/menu")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<MenuResponseDto>>() {});

            if (ownerMenus == null) {
                return List.of();
            }

            return ownerMenus.stream()
                    .map(this::attachImageProxyUrl)
                    .toList();

        } catch (Exception e) {
            throw new RuntimeException("사장 서버 메뉴 목록 조회 실패", e);
        }
    }

    private MenuResponseDto attachImageProxyUrl(MenuResponseDto menu) {
        String imagePath = menu.getMenuImage();

        String imageUrl = null;

        if (imagePath != null && !imagePath.isBlank()) {
            String encodedPath = URLEncoder.encode(imagePath, StandardCharsets.UTF_8);
            imageUrl = "/api/menu-images?path=" + encodedPath;
        }

        return MenuResponseDto.builder()
                .menuId(menu.getMenuId())
                .menuName(menu.getMenuName())
                .menuPrice(menu.getMenuPrice())
                .menuImage(menu.getMenuImage())
                .imageUrl(imageUrl)
                .build();
    }
}