package com.example.cafeordersystem.menu.read;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuReadMapper {

    List<MenuReadRow> findActiveMenus();

    void upsertMenu(MenuReadRow row);

    void markDeleted(@Param("menuId") Long menuId);
}