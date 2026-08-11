package com.shinwonjin.traveldiary.dto.trip;

import jakarta.validation.constraints.Size;

public record TripPhotoMemoUpdateRequest(
        @Size(max = 1000, message = "사진 메모는 1000자 이하로 입력해 주세요.")
        String memo
) {
}