package com.shinwonjin.traveldiary.dto.trip;

public record TripSummaryResponse(
        long totalCount,
        long ownedCount,
        long participatingCount
) {
}