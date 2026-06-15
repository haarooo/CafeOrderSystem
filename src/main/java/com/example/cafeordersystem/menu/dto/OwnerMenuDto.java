package com.example.cafeordersystem.menu.dto;

import lombok.Data;

/**
 * 사장 서버 GET /api/menu 응답을 받기 위한 DTO.
 */
@Data
public class OwnerMenuDto {

    private Long menuId;
    private String menuName;
    private Integer menuPrice;
    private String menuImage;
}
