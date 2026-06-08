package com.example.cafeordersystem.order.read;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReviewableOrderReadMapper {

    int existsReviewableOrder(@Param("orderId") Long orderId);

    void upsertOrder(ReviewableOrderReadRow row);

    void markReviewWritten(@Param("orderId") Long orderId);
}