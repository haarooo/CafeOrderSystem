package com.example.cafeordersystem.reply.service;

import com.example.cafeordersystem.reply.dto.ReviewReplyResponseDto;
import com.example.cafeordersystem.review.dto.ReviewRow;
import com.example.cafeordersystem.review.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 고객 화면에서 사장 답글을 조회하는 서비스.
 *
 * 변경된 구조:
 * - reply_read 테이블을 조회하지 않는다.
 * - 구매 서버 review 테이블이 리뷰 + 분석결과 + 답글 원장이다.
 * - 고객 화면은 review.reply_content / reply_status를 기준으로 답글을 조회한다.
 */
@Service
@RequiredArgsConstructor
public class ReviewReplyService {

    private final ReviewMapper reviewMapper;

    @Transactional(readOnly = true)
    public ReviewReplyResponseDto getReply(Long reviewId) {
        if (reviewId == null) {
            throw new RuntimeException("리뷰 ID는 필수입니다.");
        }

        ReviewRow review = reviewMapper.findByReviewId(reviewId);

        if (review == null) {
            throw new RuntimeException("리뷰를 찾을 수 없습니다. reviewId=" + reviewId);
        }

        if (!"ACTIVE".equals(review.getReplyStatus()) || isBlank(review.getReplyContent())) {
            return ReviewReplyResponseDto.builder()
                    .customerReviewId(reviewId)
                    .orderId(review.getOrderId())
                    .hasReply(false)
                    .message("아직 등록된 답글이 없습니다.")
                    .build();
        }

        return ReviewReplyResponseDto.builder()
                .customerReviewId(review.getReviewId())
                .orderId(review.getOrderId())
                .hasReply(true)
                .replyContent(review.getReplyContent())
                .repliedAt(review.getRepliedAt())
                .message("답글 조회 성공")
                .build();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}