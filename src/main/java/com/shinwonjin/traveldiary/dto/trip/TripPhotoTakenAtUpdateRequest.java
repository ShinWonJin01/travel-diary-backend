package com.shinwonjin.traveldiary.dto.trip;

import java.time.LocalDateTime;

public record TripPhotoTakenAtUpdateRequest(
        LocalDateTime takenAt
) {
}