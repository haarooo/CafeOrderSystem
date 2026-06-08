package com.example.cafeordersystem.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사장 서버에서 발행할 menu.created / menu.updated / menu.deleted 이벤트 payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuEventPayload {

    private Long menuId;

    private String menuName;

    private Integer menuPrice;

    private String menuImage;

}