package com.example.cafeordersystem.review.controller;

import com.example.cafeordersystem.review.dto.ReviewCreateRequestDto;
import com.example.cafeordersystem.review.dto.ReviewResponseDto;
import com.example.cafeordersystem.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(
            @RequestBody ReviewCreateRequestDto request
    ) {
        return ResponseEntity.ok(reviewService.createReview(request));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> getReview(
            @PathVariable Long reviewId
    ) {
        return ResponseEntity.ok(reviewService.getReview(reviewId));
    }
}