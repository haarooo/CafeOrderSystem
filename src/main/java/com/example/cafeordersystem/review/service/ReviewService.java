package com.example.cafeordersystem.review.service;

import com.example.cafeordersystem.review.dto.ReviewCreateRequestDto;
import com.example.cafeordersystem.review.dto.ReviewListItemDto;
import com.example.cafeordersystem.review.dto.ReviewResponseDto;
import com.example.cafeordersystem.review.dto.ReviewRow;
import com.example.cafeordersystem.review.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;

    public ReviewResponseDto createReview(ReviewCreateRequestDto request) {
        validateCreateReviewRequest(request);

        if (reviewMapper.existsByOrderId(request.getOrderId()) > 0) {
            throw new RuntimeException("이미 해당 주문에 리뷰가 존재합니다.");
        }

        reviewMapper.insertReview(request);

        ReviewRow savedReview = reviewMapper.findByOrderId(request.getOrderId());

        return ReviewResponseDto.createSuccess(
                savedReview,
                "리뷰가 성공적으로 작성되었습니다."
        );
    }

    @Transactional(readOnly = true)
    public ReviewResponseDto getReview(Long reviewId) {
        ReviewRow review = reviewMapper.findByReviewId(reviewId);

        if (review == null) {
            throw new RuntimeException("리뷰를 찾을 수 없습니다: " + reviewId);
        }

        return ReviewResponseDto.from(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewRow> getReviewRowsByPage(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 20);
        int offset = safePage * safeSize;

        return reviewMapper.findReviewsByPaging(offset, safeSize);
    }

    @Transactional(readOnly = true)
    public long countReviews() {
        return reviewMapper.countReviews();
    }

    @Transactional(readOnly = true)
    public List<ReviewListItemDto> getOwnerReviewList(int page) {
        int safePage = Math.max(page, 0);
        int offset = safePage * 10;
        return reviewMapper.findOwnerReviewList(offset, 10);
    }

    private void validateCreateReviewRequest(ReviewCreateRequestDto request) {
        if (request.getOrderId() == null) {
            throw new RuntimeException("주문 ID는 필수입니다.");
        }

        if (request.getReviewContent() == null || request.getReviewContent().isBlank()) {
            throw new RuntimeException("리뷰 내용은 필수입니다.");
        }
    }


}