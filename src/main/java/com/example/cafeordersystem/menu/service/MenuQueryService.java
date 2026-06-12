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
 *
 * 이미지 처리 정책:
 * - S3 전체 URL이면 그대로 내려준다.
 * - 기존 /uploads/... 경로이면 구매 서버 이미지 프록시를 사용한다.
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
        String imageUrl = resolveImageUrl(row.getMenuImage());

        return MenuItemDto.builder()
                .menuId(row.getMenuId())
                .menuName(row.getMenuName())
                .menuPrice(row.getMenuPrice())
                .menuImage(row.getMenuImage())
                .imageUrl(imageUrl)
                .build();
    }

    private String resolveImageUrl(String menuImage) {
        if (menuImage == null || menuImage.isBlank()) {
            return null;
        }

        String trimmed = menuImage.trim();

        /*
         * 사장 서버가 S3에 업로드하고 DB에 전체 URL을 저장한 경우.
         * 예:
         * https://bucket-name.s3.ap-northeast-2.amazonaws.com/menu-images/americano.png
         *
         * 이 경우 구매 서버가 /api/menu-images 프록시로 감싸면 안 된다.
         * 브라우저가 S3 URL을 직접 불러오게 그대로 내려준다.
         */
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }

        /*
         * 기존 로컬 업로드 방식.
         * 예:
         * /uploads/images/americano.png
         *
         * 이 경우에만 구매 서버 이미지 프록시를 사용한다.
         */
        if (trimmed.startsWith("/uploads/")) {
            String encodedPath = URLEncoder.encode(trimmed, StandardCharsets.UTF_8);
            return "/api/menu-images?path=" + encodedPath;
        }

        /*
         * 그 외 상대 경로는 그대로 내려준다.
         */
        return trimmed;
    }
}