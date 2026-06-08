package com.example.cafeordersystem.menu.controller;

import com.example.cafeordersystem.menu.dto.MenuResponseDto;
import com.example.cafeordersystem.menu.service.MenuQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * React POS 화면이 호출하는 구매 서버 메뉴 API.
 *
 * 프론트는 /api/menus만 호출한다.
 * 실제 메뉴 데이터는 구매 서버가 사장 서버 /api/menu에서 가져온다.
 */
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuQueryService menuQueryService;

    @GetMapping
    public List<MenuResponseDto> getMenus() {
        return menuQueryService.getMenus();
    }
}