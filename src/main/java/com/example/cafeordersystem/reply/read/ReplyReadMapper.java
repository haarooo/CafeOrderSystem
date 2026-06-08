package com.example.cafeordersystem.reply.read;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReplyReadMapper {

    ReplyReadRow findByCustomerReviewId(@Param("customerReviewId") Long customerReviewId);

    void upsertReply(ReplyReadRow row);

    void markDeleted(@Param("customerReviewId") Long customerReviewId);
}