package com.example.cafeordersystem.reply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReviewReplyQueryRequestEvent {

    private String requestId;
    private Long customerReviewId;
}