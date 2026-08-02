package com.shinwonjin.traveldiary.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberProfileUpdateRequest(

        @NotBlank(message = "이름을 입력해 주세요.")
        @Size(
                max = 50,
                message = "이름은 50자 이하로 입력해 주세요."
        )
        String name,

        @NotBlank(message = "닉네임을 입력해 주세요.")
        @Size(
                min = 2,
                max = 20,
                message = "닉네임은 2자 이상 20자 이하로 입력해 주세요."
        )
        String nickname
) {
}