package com.example.cafeordersystem.review.reply.service;

import com.example.cafeordersystem.global.outbox.OutboxService;
import com.example.cafeordersystem.review.dto.ReviewRow;
import com.example.cafeordersystem.review.mapper.ReviewMapper;
import com.example.cafeordersystem.review.reply.dto.ReviewRepliedPayload;
import com.example.cafeordersystem.review.reply.dto.ReviewReplyCommandRequestEvent;
import com.example.cafeordersystem.review.reply.dto.ReviewReplyCommandResultEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewReplyCommandService {

    private static final String COMMAND_CREATE = "CREATE";
    private static final String COMMAND_UPDATE = "UPDATE";
    private static final String COMMAND_DELETE = "DELETE";

    private static final String REVIEW_REPLIED_TOPIC = "review.replied";
    private static final String REVIEW_REPLIED_EVENT_TYPE = "review.replied";

    private static final String REVIEW_REPLY_UPDATED_TOPIC = "review.reply.updated";
    private static final String REVIEW_REPLY_UPDATED_EVENT_TYPE = "review.reply.updated";

    private static final String REVIEW_REPLY_DELETED_TOPIC = "review.reply.deleted";
    private static final String REVIEW_REPLY_DELETED_EVENT_TYPE = "review.reply.deleted";

    private static final String REVIEW_AGGREGATE_TYPE = "REVIEW";

    private final ReviewMapper reviewMapper;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ReviewReplyCommandResultEvent handle(ReviewReplyCommandRequestEvent request) {
        String commandType = normalizeCommandType(request.getCommandType());

        if (COMMAND_CREATE.equals(commandType)) {
            return createReply(request);
        }

        if (COMMAND_UPDATE.equals(commandType)) {
            return updateReply(request);
        }

        if (COMMAND_DELETE.equals(commandType)) {
            return deleteReply(request);
        }

        return fail(request, "지원하지 않는 답글 명령입니다. commandType=" + request.getCommandType());
    }

    private ReviewReplyCommandResultEvent createReply(ReviewReplyCommandRequestEvent request) {
        if (isBlank(request.getReplyContent())) {
            return fail(request, "답글 내용은 필수입니다.");
        }

        ReviewRow review = reviewMapper.findByReviewId(request.getReviewId());

        if (review == null) {
            return fail(request, "리뷰를 찾을 수 없습니다. reviewId=" + request.getReviewId());
        }

        if ("ACTIVE".equals(review.getReplyStatus())) {
            return fail(request, "이미 답글이 등록된 리뷰입니다.");
        }

        int updated = reviewMapper.updateReply(
                request.getReviewId(),
                request.getReplyContent()
        );

        if (updated != 1) {
            return fail(request, "답글 등록에 실패했습니다.");
        }

        ReviewRow updatedReview = reviewMapper.findByReviewId(request.getReviewId());

        saveReviewReplyOutbox(
                REVIEW_REPLIED_TOPIC,
                REVIEW_REPLIED_EVENT_TYPE,
                updatedReview
        );

        return success(request, updatedReview, "답글이 등록되었습니다.");
    }

    private ReviewReplyCommandResultEvent updateReply(ReviewReplyCommandRequestEvent request) {
        if (isBlank(request.getReplyContent())) {
            return fail(request, "수정할 답글 내용은 필수입니다.");
        }

        ReviewRow review = reviewMapper.findByReviewId(request.getReviewId());

        if (review == null) {
            return fail(request, "리뷰를 찾을 수 없습니다. reviewId=" + request.getReviewId());
        }

        if (!"ACTIVE".equals(review.getReplyStatus())) {
            return fail(request, "등록된 답글이 없어 수정할 수 없습니다.");
        }

        int updated = reviewMapper.updateReplyContent(
                request.getReviewId(),
                request.getReplyContent()
        );

        if (updated != 1) {
            return fail(request, "답글 수정에 실패했습니다.");
        }

        ReviewRow updatedReview = reviewMapper.findByReviewId(request.getReviewId());

        saveReviewReplyOutbox(
                REVIEW_REPLY_UPDATED_TOPIC,
                REVIEW_REPLY_UPDATED_EVENT_TYPE,
                updatedReview
        );

        return success(request, updatedReview, "답글이 수정되었습니다.");
    }

    private ReviewReplyCommandResultEvent deleteReply(ReviewReplyCommandRequestEvent request) {
        ReviewRow review = reviewMapper.findByReviewId(request.getReviewId());

        if (review == null) {
            return fail(request, "리뷰를 찾을 수 없습니다. reviewId=" + request.getReviewId());
        }

        if (!"ACTIVE".equals(review.getReplyStatus())) {
            return fail(request, "삭제할 답글이 없습니다.");
        }

        int updated = reviewMapper.deleteReply(request.getReviewId());

        if (updated != 1) {
            return fail(request, "답글 삭제에 실패했습니다.");
        }

        ReviewRow updatedReview = reviewMapper.findByReviewId(request.getReviewId());

        saveReviewReplyOutbox(
                REVIEW_REPLY_DELETED_TOPIC,
                REVIEW_REPLY_DELETED_EVENT_TYPE,
                updatedReview
        );

        return success(request, updatedReview, "답글이 삭제되었습니다.");
    }

    private void saveReviewReplyOutbox(
            String topic,
            String eventType,
            ReviewRow review
    ) {
        ReviewRepliedPayload payload = ReviewRepliedPayload.builder()
                .reviewId(review.getReviewId())
                .orderId(review.getOrderId())
                .replyContent(review.getReplyContent())
                .replyStatus(review.getReplyStatus())
                .repliedAt(review.getRepliedAt())
                .replyUpdatedAt(review.getReplyUpdatedAt())
                .build();

        JsonNode payloadNode = objectMapper.valueToTree(payload);

        outboxService.saveEvent(
                topic,
                eventType,
                REVIEW_AGGREGATE_TYPE,
                String.valueOf(review.getReviewId()),
                payloadNode
        );
    }

    private ReviewReplyCommandResultEvent success(
            ReviewReplyCommandRequestEvent request,
            ReviewRow review,
            String message
    ) {
        return ReviewReplyCommandResultEvent.builder()
                .requestId(request.getRequestId())
                .commandType(normalizeCommandType(request.getCommandType()))
                .reviewId(review.getReviewId())
                .orderId(review.getOrderId())
                .replyContent(review.getReplyContent())
                .replyStatus(review.getReplyStatus())
                .repliedAt(review.getRepliedAt())
                .replyUpdatedAt(review.getReplyUpdatedAt())
                .success(true)
                .message(message)
                .build();
    }

    private ReviewReplyCommandResultEvent fail(
            ReviewReplyCommandRequestEvent request,
            String message
    ) {
        return ReviewReplyCommandResultEvent.builder()
                .requestId(request == null ? null : request.getRequestId())
                .commandType(request == null ? null : normalizeCommandType(request.getCommandType()))
                .reviewId(request == null ? null : request.getReviewId())
                .orderId(request == null ? null : request.getOrderId())
                .replyContent(request == null ? null : request.getReplyContent())
                .success(false)
                .message(message)
                .build();
    }

    private String normalizeCommandType(String commandType) {
        if (commandType == null) {
            return "";
        }

        return commandType.trim().toUpperCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}