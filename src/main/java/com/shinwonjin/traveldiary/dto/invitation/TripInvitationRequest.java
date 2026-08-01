package com.shinwonjin.traveldiary.dto.invitation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TripInvitationRequest(

        @NotBlank(
                message = "초대할 사용자의 닉네임을 입력해 주세요."
        )
        @Size(
                max = 20,
                message = "닉네임은 20자 이하로 입력해 주세요."
        )
        String nickname

) {
}