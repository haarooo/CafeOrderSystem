package com.example.cafeordersystem.reply.service;


import com.example.cafeordersystem.reply.dto.ReviewReplyQueryResultEvent;
import com.example.cafeordersystem.reply.dto.ReviewReplyResponseDto;
import com.example.cafeordersystem.reply.kafka.ReviewReplyKafkaClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewReplyService {

    private final ReviewReplyKafkaClient replyKafkaClient;

    public ReviewReplyResponseDto getReply(Long customerReviewId) {
        if (customerReviewId == null) {
            throw new RuntimeException("고객 리뷰 ID는 필수입니다.");
        }

        ReviewReplyQueryResultEvent result =
                replyKafkaClient.requestReply(customerReviewId);

        return ReviewReplyResponseDto.from(result);
    }
}