package com.example.cafeordersystem.global.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class SpaWebFilter extends OncePerRequestFilter {

    /*
     * React Router 새로고침 대응 필터
     *
     * 예:
     * - /
     * - /pos
     * - /receipt?orderId=1
     * - /review/write?orderId=1
     *
     * 위 주소들은 백엔드 API가 아니라 React 화면 경로입니다.
     * 따라서 Spring Boot가 404를 내지 않고 index.html로 넘겨야 합니다.
     *
     * 반대로 아래 주소들은 백엔드나 정적 리소스라서 필터링하면 안 됩니다.
     * - /api/**
     * - /qrcodes/**
     * - /assets/**
     * - /swagger-ui/**
     * - /v3/api-docs/**
     */

    private static final List<String> EXCLUDE_PREFIXES = List.of(
            "/api",
            "/qrcodes",
            "/assets",
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator"
    );

    private static final List<String> EXCLUDE_EXACT_PATHS = List.of(
            "/index.html",
            "/favicon.ico",
            "/favicon.svg",
            "/robots.txt",
            "/manifest.json"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (shouldForwardToReact(path)) {
            request.getRequestDispatcher("/index.html").forward(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldForwardToReact(String path) {
        if (path == null || path.isBlank()) {
            return true;
        }

        if (EXCLUDE_EXACT_PATHS.contains(path)) {
            return false;
        }

        for (String prefix : EXCLUDE_PREFIXES) {
            if (path.startsWith(prefix)) {
                return false;
            }
        }

        /*
         * .js, .css, .png, .svg, .ico 같은 실제 파일 요청은 제외한다.
         * 예:
         * /assets/index-xxx.js
         * /assets/index-xxx.css
         * /icons.svg
         */
        if (path.contains(".")) {
            return false;
        }

        return true;
    }
}