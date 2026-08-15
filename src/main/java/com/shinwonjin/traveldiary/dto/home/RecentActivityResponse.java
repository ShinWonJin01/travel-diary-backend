package com.shinwonjin.traveldiary.dto.home;

import java.time.LocalDateTime;

public record RecentActivityResponse(
        Long tripId,
        String tripTitle,
        String actorNickname,
        int photoCount,
        LocalDateTime createdAt
) {
}