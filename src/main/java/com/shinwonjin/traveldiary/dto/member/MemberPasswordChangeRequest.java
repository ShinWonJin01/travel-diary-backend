package com.shinwonjin.traveldiary.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberPasswordChangeRequest(

        @NotBlank(message = "현재 비밀번호를 입력해 주세요.")
        String currentPassword,

        @NotBlank(message = "새 비밀번호를 입력해 주세요.")
        @Size(
                min = 8,
                max = 30,
                message = "새 비밀번호는 8자 이상 30자 이하로 입력해 주세요."
        )
        String newPassword
) {
}