package com.example.cafeordersystem.menu.controller;

import com.example.cafeordersystem.menu.dto.MenuItemDto;
import com.example.cafeordersystem.menu.service.MenuQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * React POS가 호출하는 메뉴 API.
 *
 * React → 구매 서버 /api/menus
 * 구매 서버 → Kafka → 사장 서버 메뉴 조회
 */
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuQueryService menuQueryService;

    @GetMapping
    public List<MenuItemDto> getMenus() {
        return menuQueryService.getMenus();
    }
}