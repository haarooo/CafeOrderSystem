package com.example.cafeordersystem.global.event;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProcessedEventMapper {

    int insertProcessedEvent(
            @Param("eventId") String eventId,
            @Param("eventType") String eventType
    );
}