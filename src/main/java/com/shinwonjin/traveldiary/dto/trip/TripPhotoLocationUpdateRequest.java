package com.shinwonjin.traveldiary.dto.trip;

public record TripPhotoLocationUpdateRequest(
        Double latitude,
        Double longitude,
        String locationName
) {
}