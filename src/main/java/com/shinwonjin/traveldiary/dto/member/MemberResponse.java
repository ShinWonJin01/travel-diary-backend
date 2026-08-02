package com.shinwonjin.traveldiary.dto.member;

import java.time.LocalDateTime;

import com.shinwonjin.traveldiary.entity.Member;

public record MemberResponse(
        Long id,
        String name,
        String email,
        String nickname,
        String profileImagePath,
        LocalDateTime createdAt
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getNickname(),
                member.getProfileImagePath(),
                member.getCreatedAt()
        );
    }
}