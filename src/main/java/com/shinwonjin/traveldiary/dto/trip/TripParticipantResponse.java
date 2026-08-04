package com.shinwonjin.traveldiary.dto.trip;

import com.shinwonjin.traveldiary.entity.Member;
import com.shinwonjin.traveldiary.entity.TripMember;
import com.shinwonjin.traveldiary.entity.TripMemberRole;

public record TripParticipantResponse(
        Long memberId,
        String nickname,
        String profileImagePath,
        TripMemberRole role
) {

    public static TripParticipantResponse from(
            TripMember tripMember
    ) {
        Member member = tripMember.getMember();

        return new TripParticipantResponse(
                member.getId(),
                member.getNickname(),
                member.getProfileImagePath(),
                tripMember.getRole()
        );
    }
}