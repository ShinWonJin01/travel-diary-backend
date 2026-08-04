package com.shinwonjin.traveldiary.dto.trip;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TripUpdateRequest(

        @NotBlank(message = "여행 제목을 입력해 주세요.")
        @Size(max = 100, message = "여행 제목은 100자 이하여야 합니다.")
        String title,

        @NotBlank(message = "대표 지역을 입력해 주세요.")
        @Size(max = 100, message = "대표 지역은 100자 이하여야 합니다.")
        String destination,

        @NotNull(message = "여행 시작일을 선택해 주세요.")
        LocalDate startDate,

        LocalDate endDate,

        @Size(max = 1000, message = "여행 소개는 1000자 이하여야 합니다.")
        String description
) {
}