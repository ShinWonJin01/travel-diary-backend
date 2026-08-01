package com.shinwonjin.traveldiary.dto.member;

import jakarta.validation.constraints.NotBlank;

public record MemberDeleteRequest(

        @NotBlank(message = "비밀번호를 입력해 주세요.")
        String password
) {
}