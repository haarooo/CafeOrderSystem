package com.example.cafeordersystem.global.outbox;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OutboxMapper {

    void insertOutboxEvent(OutboxEvent event);

    List<OutboxEvent> findNewEvents(@Param("limit") int limit);

    void markAsSent(@Param("id") Long id);

    void markAsFailed(
            @Param("id") Long id,
            @Param("lastError") String lastError
    );
}