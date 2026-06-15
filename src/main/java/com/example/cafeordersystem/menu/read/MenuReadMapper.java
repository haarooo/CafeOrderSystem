package com.example.cafeordersystem.menu.read;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuReadMapper {

    List<MenuReadRow> findActiveMenus();

    void upsertMenu(MenuReadRow row);

    void markDeleted(@Param("menuId") Long menuId);

    /**
     * 전체 스냅샷 반영 전에 기존 메뉴를 일괄 비활성화한다.
     * 이후 현재 사장 서버에 존재하는 메뉴만 upsertMenu로 다시 활성화한다.
     */
    void markAllDeleted();
}
