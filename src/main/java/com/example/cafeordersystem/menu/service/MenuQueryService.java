package com.example.cafeordersystem.menu.service;

import com.example.cafeordersystem.menu.dto.MenuItemDto;
import com.example.cafeordersystem.menu.dto.MenuListQueryResultEvent;
import com.example.cafeordersystem.menu.kafka.MenuKafkaClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/*
 * React POS 화면에 내려줄 메뉴 목록을 준비하는 서비스.
 * 실제 메뉴 데이터는 Kafka로 사장 서버에 요청한다.
 */
@Service
@RequiredArgsConstructor
public class MenuQueryService {

    private final MenuKafkaClient menuKafkaClient;

    public List<MenuItemDto> getMenus() {
        MenuListQueryResultEvent result = menuKafkaClient.requestMenus();

        if (result.getMenus() == null) {
            return List.of();
        }

        return result.getMenus().stream()
                .map(this::attachImageProxyUrl)
                .toList();
    }

    /**
     * 사장 서버에서 받은 menuImage 경로를 구매 서버 프록시 이미지 URL로 변환한다.
     *
     * 사장 서버 원본:
     * /uploads/images/abc.png
     *
     * React POS 사용:
     * /api/menu-images?path=%2Fuploads%2Fimages%2Fabc.png
     */
    private MenuItemDto attachImageProxyUrl(MenuItemDto menu) {
        String imagePath = menu.getMenuImage();

        String imageUrl = null;

        if (imagePath != null && !imagePath.isBlank()) {
            String encodedPath = URLEncoder.encode(imagePath, StandardCharsets.UTF_8);
            imageUrl = "/api/menu-images?path=" + encodedPath;
        }

        return MenuItemDto.builder()
                .menuId(menu.getMenuId())
                .menuName(menu.getMenuName())
                .menuPrice(menu.getMenuPrice())
                .menuImage(menu.getMenuImage())
                .imageUrl(imageUrl)
                .build();
    }
}