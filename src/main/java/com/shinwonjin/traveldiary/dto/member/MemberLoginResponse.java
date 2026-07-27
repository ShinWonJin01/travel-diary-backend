package com.shinwonjin.traveldiary.dto.member;

public record MemberLoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        MemberResponse member
) {
}