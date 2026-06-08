package com.example.cafeordersystem.menu.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

/**
 * React POS가 메뉴 이미지를 볼 때 사용하는 이미지 프록시.
 *
 * React는 구매 서버 /api/menu-images만 호출한다.
 * 구매 서버가 내부적으로 사장 서버 /uploads/images/... 이미지를 가져와서 전달한다.
 */
@Slf4j
@RestController
@RequestMapping("/api/menu-images")
public class MenuImageProxyController {

    private final RestClient restClient;

    public MenuImageProxyController(
            @Value("${owner.server.base-url}") String ownerServerBaseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(ownerServerBaseUrl)
                .build();
    }

    @GetMapping
    public ResponseEntity<byte[]> getMenuImage(@RequestParam String path) {
        if (!isValidImagePath(path)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            ResponseEntity<byte[]> ownerResponse = restClient.get()
                    .uri(path)
                    .retrieve()
                    .toEntity(byte[].class);

            MediaType contentType = ownerResponse.getHeaders().getContentType();

            return ResponseEntity.ok()
                    .contentType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM)
                    .body(ownerResponse.getBody());

        } catch (Exception e) {
            log.error("메뉴 이미지 조회 실패 path={}", path, e);
            return ResponseEntity.notFound().build();
        }
    }

    private boolean isValidImagePath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }

        if (!path.startsWith("/uploads/")) {
            return false;
        }

        if (path.contains("..")) {
            return false;
        }

        if (path.contains("\\")) {
            return false;
        }

        if (path.startsWith("http://") || path.startsWith("https://")) {
            return false;
        }

        return true;
    }
}