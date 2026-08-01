package com.shinwonjin.traveldiary.dto.invitation;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.shinwonjin.traveldiary.entity.Trip;
import com.shinwonjin.traveldiary.entity.TripMember;
import com.shinwonjin.traveldiary.entity.TripMemberStatus;

public record TripInvitationResponse(

        Long invitationId,

        Long tripId,
        String tripTitle,

        LocalDate startDate,
        LocalDate endDate,

        Long inviterId,
        String inviterNickname,

        Long inviteeId,
        String inviteeNickname,

        long currentParticipantCount,

        TripMemberStatus status,

        LocalDateTime createdAt

) {

    public static TripInvitationResponse from(
            TripMember tripMember,
            long currentParticipantCount
    ) {
        Trip trip = tripMember.getTrip();

        return new TripInvitationResponse(
                tripMember.getId(),

                trip.getId(),
                trip.getTitle(),

                trip.getStartDate(),
                trip.getEndDate(),

                trip.getOwner().getId(),
                trip.getOwner().getNickname(),

                tripMember.getMember().getId(),
                tripMember.getMember().getNickname(),

                currentParticipantCount,

                tripMember.getStatus(),

                tripMember.getCreatedAt()
        );
    }
}