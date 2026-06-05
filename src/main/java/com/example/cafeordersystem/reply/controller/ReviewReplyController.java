package com.example.cafeordersystem.reply.controller;


import com.example.cafeordersystem.reply.dto.ReviewReplyResponseDto;
import com.example.cafeordersystem.reply.service.ReviewReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewReplyController {

    private final ReviewReplyService reviewReplyService;

    @GetMapping("/{reviewId}/reply")
    public ResponseEntity<ReviewReplyResponseDto> getReply(
            @PathVariable Long reviewId
    ) {
        return ResponseEntity.ok(
                reviewReplyService.getReply(reviewId)
        );
    }
}