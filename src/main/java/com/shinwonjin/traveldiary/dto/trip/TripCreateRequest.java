package com.shinwonjin.traveldiary.dto.trip;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TripCreateRequest(

        @NotBlank(message = "여행 제목은 필수입니다.")
        @Size(max = 100, message = "여행 제목은 100자 이하여야 합니다.")
        String title,

        @NotBlank(message = "여행지는 필수입니다.")
        @Size(max = 100, message = "여행지는 100자 이하여야 합니다.")
        String destination,

        @NotNull(message = "여행 시작일은 필수입니다.")
        LocalDate startDate,

        @NotNull(message = "여행 종료일은 필수입니다.")
        LocalDate endDate,

        @Size(max = 1000, message = "여행 설명은 1000자 이하여야 합니다.")
        String description

) {
}