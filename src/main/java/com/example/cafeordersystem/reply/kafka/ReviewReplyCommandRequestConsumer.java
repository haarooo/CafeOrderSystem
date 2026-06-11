package com.example.cafeordersystem.reply.kafka;

import com.example.cafeordersystem.review.reply.dto.ReviewReplyCommandRequestEvent;
import com.example.cafeordersystem.review.reply.dto.ReviewReplyCommandResultEvent;
import com.example.cafeordersystem.review.reply.service.ReviewReplyCommandService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewReplyCommandRequestConsumer {

    private final ObjectMapper objectMapper;
    private final ReviewReplyCommandService reviewReplyCommandService;
    private final ReviewReplyCommandResultProducer resultProducer;

    @KafkaListener(
            topics = "review.reply.command.request",
            groupId = "customer-review-reply-command-service"
    )
    public void consume(String message) {
        ReviewReplyCommandRequestEvent request = null;

        try {
            log.info("답글 명령 요청 수신: {}", message);

            request = objectMapper.readValue(
                    message,
                    ReviewReplyCommandRequestEvent.class
            );

            ReviewReplyCommandResultEvent result =
                    reviewReplyCommandService.handle(request);

            resultProducer.send(result);

        } catch (Exception e) {
            log.error("답글 명령 요청 처리 실패 message={}", message, e);

            if (request != null) {
                ReviewReplyCommandResultEvent failResult =
                        ReviewReplyCommandResultEvent.builder()
                                .requestId(request.getRequestId())
                                .commandType(request.getCommandType())
                                .reviewId(request.getReviewId())
                                .orderId(request.getOrderId())
                                .replyContent(request.getReplyContent())
                                .success(false)
                                .message("답글 명령 처리 중 오류가 발생했습니다.")
                                .build();

                resultProducer.send(failResult);
            }
        }
    }
}