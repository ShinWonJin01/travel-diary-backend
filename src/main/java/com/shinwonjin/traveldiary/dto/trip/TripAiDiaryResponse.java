package com.shinwonjin.traveldiary.dto.trip;

import com.shinwonjin.traveldiary.entity.TripAiDiary;

import java.time.LocalDateTime;

public record TripAiDiaryResponse(
        Long id,
        Long tripId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static TripAiDiaryResponse from(TripAiDiary diary) {
        return new TripAiDiaryResponse(
                diary.getId(),
                diary.getTrip().getId(),
                diary.getContent(),
                diary.getCreatedAt(),
                diary.getUpdatedAt()
        );
    }
}