package com.example.cafeordersystem.menu.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponseDto {

    private Long menuId;
    private String menuName;
    private Integer menuPrice;
    private String menuImage;
    private String imageUrl;
}