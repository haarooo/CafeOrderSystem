package com.example.cafeordersystem.review.mapper;

import com.example.cafeordersystem.review.dto.ReviewCreateRequestDto;
import com.example.cafeordersystem.review.dto.ReviewRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReviewMapper {

    int existsByOrderId(@Param("orderId") Long orderId);

    void insertReview(ReviewCreateRequestDto request);

    ReviewRow findByOrderId(@Param("orderId") Long orderId);

    ReviewRow findByReviewId(@Param("reviewId") Long reviewId);

    List<ReviewRow> findReviewsByPaging(
            @Param("offset") int offset,
            @Param("size") int size
    );

    long countReviews();

    List<ReviewRow> findAnalysisTargets(
            @Param("failedRetryDeadline") LocalDateTime failedRetryDeadline,
            @Param("limit") int limit
    );

    void markAnalysisProcessing(@Param("reviewId") Long reviewId);

    void markAnalysisCompleted(
            @Param("reviewId") Long reviewId,
            @Param("analysisResultJson") String analysisResultJson
    );

    void markAnalysisFailed(@Param("reviewId") Long reviewId);

    int resetStaleProcessing(
            @Param("processingTimeoutDeadline") LocalDateTime processingTimeoutDeadline
    );
}