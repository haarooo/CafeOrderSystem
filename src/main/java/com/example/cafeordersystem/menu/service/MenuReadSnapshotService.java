package com.example.cafeordersystem.menu.service;

import com.example.cafeordersystem.menu.dto.OwnerMenuDto;
import com.example.cafeordersystem.menu.read.MenuReadMapper;
import com.example.cafeordersystem.menu.read.MenuReadRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사장 서버 메뉴 전체 목록을 구매 서버 menu_read에 원자적으로 반영한다.
 *
 * 처리 방식:
 * 1. 응답 전체를 먼저 검증하고 MenuReadRow로 변환
 * 2. 기존 menu_read를 삭제하지 않고 일단 deleted=true 처리
 * 3. 사장 서버에 현재 존재하는 메뉴를 UPSERT하며 deleted=false 복구
 *
 * 하나의 트랜잭션으로 처리하므로 조회 화면에 중간 상태가 노출되지 않는다.
 */
@Service
@RequiredArgsConstructor
public class MenuReadSnapshotService {

    private final MenuReadMapper menuReadMapper;

    @Transactional
    public int replaceSnapshot(List<OwnerMenuDto> ownerMenus) {
        if (ownerMenus == null) {
            throw new IllegalArgumentException("사장 서버 메뉴 응답이 null입니다.");
        }

        // DB를 변경하기 전에 전체 데이터부터 검증한다.
        List<MenuReadRow> rows = ownerMenus.stream()
                .map(this::toMenuReadRow)
                .toList();

        // 사장 서버에서 사라진 메뉴는 구매 화면에서도 비활성화한다.
        menuReadMapper.markAllDeleted();

        // 현재 사장 서버에 존재하는 메뉴는 생성 또는 갱신한다.
        for (MenuReadRow row : rows) {
            menuReadMapper.upsertMenu(row);
        }

        return rows.size();
    }

    private MenuReadRow toMenuReadRow(OwnerMenuDto menu) {
        if (menu == null) {
            throw new IllegalArgumentException("메뉴 항목이 null입니다.");
        }

        if (menu.getMenuId() == null) {
            throw new IllegalArgumentException("menuId가 없는 메뉴가 있습니다.");
        }

        if (menu.getMenuName() == null || menu.getMenuName().isBlank()) {
            throw new IllegalArgumentException(
                    "menuName이 없는 메뉴가 있습니다. menuId=" + menu.getMenuId()
            );
        }

        if (menu.getMenuPrice() == null || menu.getMenuPrice() < 0) {
            throw new IllegalArgumentException(
                    "menuPrice가 올바르지 않습니다. menuId=" + menu.getMenuId()
            );
        }

        MenuReadRow row = new MenuReadRow();
        row.setMenuId(menu.getMenuId());
        row.setMenuName(menu.getMenuName());
        row.setMenuPrice(menu.getMenuPrice());
        row.setMenuImage(menu.getMenuImage());
        row.setDeleted(false);
        return row;
    }
}
