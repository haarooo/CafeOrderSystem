package com.example.cafeordersystem.reply.service;

import com.example.cafeordersystem.reply.dto.ReviewReplyResponseDto;
import com.example.cafeordersystem.reply.read.ReplyReadMapper;
import com.example.cafeordersystem.reply.read.ReplyReadRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 고객 화면에서 사장 답글을 조회하는 서비스.
 *
 * 최종 구조:
 * - 사장 서버에 Kafka request/reply로 묻지 않는다.
 * - 사장 서버 reply.created/updated/deleted 이벤트로 미리 동기화된
 *   구매 서버 reply_read 테이블만 조회한다.
 */
@Service
@RequiredArgsConstructor
public class ReviewReplyService {

    private final ReplyReadMapper replyReadMapper;

    @Transactional(readOnly = true)
    public ReviewReplyResponseDto getReply(Long customerReviewId) {
        if (customerReviewId == null) {
            throw new RuntimeException("고객 리뷰 ID는 필수입니다.");
        }

        ReplyReadRow row = replyReadMapper.findByCustomerReviewId(customerReviewId);

        if (row == null || !Boolean.TRUE.equals(row.getHasReply())) {
            return ReviewReplyResponseDto.builder()
                    .customerReviewId(customerReviewId)
                    .hasReply(false)
                    .message("아직 등록된 답글이 없습니다.")
                    .build();
        }

        return ReviewReplyResponseDto.builder()
                .customerReviewId(row.getCustomerReviewId())
                .orderId(row.getOrderId())
                .hasReply(true)
                .replyContent(row.getReplyContent())
                .repliedAt(row.getRepliedAt())
                .message("답글 조회 성공")
                .build();
    }
}