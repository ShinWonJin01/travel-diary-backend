package com.shinwonjin.traveldiary.dto.trip;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.shinwonjin.traveldiary.entity.Trip;
import com.shinwonjin.traveldiary.entity.TripMemberRole;

public record TripListResponse(

        Long id,
        String title,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        String description,
        String coverImagePath,

        Long ownerId,
        String ownerNickname,

        TripMemberRole role,
        long participantCount,

        LocalDateTime createdAt

) {

    public static TripListResponse from(
            Trip trip,
            TripMemberRole role,
            long participantCount
    ) {
        return new TripListResponse(
                trip.getId(),
                trip.getTitle(),
                trip.getDestination(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getDescription(),
                trip.getCoverImagePath(),

                trip.getOwner().getId(),
                trip.getOwner().getNickname(),

                role,
                participantCount,

                trip.getCreatedAt()
        );
    }
}