package com.example.cafeordersystem.menu.read;

import lombok.Data;

@Data
public class MenuReadRow {

    private Long menuId;

    private String menuName;

    private Integer menuPrice;

    private String menuImage;


    private Boolean deleted;

    private String createdAt;

    private String updatedAt;
}