package com.shinwonjin.traveldiary.dto.trip;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.shinwonjin.traveldiary.entity.Trip;

public record TripResponse(

        Long id,
        String title,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        String description,
        String coverImagePath,
        Long ownerId,
        String ownerNickname,
        LocalDateTime createdAt

) {
    public static TripResponse from(Trip trip) {
        return new TripResponse(
                trip.getId(),
                trip.getTitle(),
                trip.getDestination(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getDescription(),
                trip.getCoverImagePath(),
                trip.getOwner().getId(),
                trip.getOwner().getNickname(),
                trip.getCreatedAt()
        );
    }
}