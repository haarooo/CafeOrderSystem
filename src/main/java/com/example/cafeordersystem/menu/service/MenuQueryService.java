package com.example.cafeordersystem.menu.service;

import com.example.cafeordersystem.menu.dto.MenuItemDto;
import com.example.cafeordersystem.menu.read.MenuReadMapper;
import com.example.cafeordersystem.menu.read.MenuReadRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * React POS 화면에 내려줄 메뉴 목록을 조회하는 서비스.
 *
 * 최종 구조:
 * - 사장 서버에 실시간으로 묻지 않는다.
 * - 사장 서버 menu.created/updated/deleted 이벤트를 받아 갱신된
 *   구매 서버 menu_read 테이블만 조회한다.
 */
@Service
@RequiredArgsConstructor
public class MenuQueryService {

    private final MenuReadMapper menuReadMapper;

    public List<MenuItemDto> getMenus() {
        return menuReadMapper.findActiveMenus().stream()
                .map(this::toMenuItemDto)
                .toList();
    }

    private MenuItemDto toMenuItemDto(MenuReadRow row) {
        String imageUrl = null;

        if (row.getMenuImage() != null && !row.getMenuImage().isBlank()) {
            String encodedPath = URLEncoder.encode(row.getMenuImage(), StandardCharsets.UTF_8);
            imageUrl = "/api/menu-images?path=" + encodedPath;
        }

        return MenuItemDto.builder()
                .menuId(row.getMenuId())
                .menuName(row.getMenuName())
                .menuPrice(row.getMenuPrice())
                .menuImage(row.getMenuImage())
                .imageUrl(imageUrl)
                .build();
    }
}