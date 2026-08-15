package com.shinwonjin.traveldiary.dto.trip;

import java.time.LocalDateTime;

import com.shinwonjin.traveldiary.entity.Notification;
import com.shinwonjin.traveldiary.entity.NotificationType;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String message,
        Long tripId,
        boolean read,
        LocalDateTime createdAt
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getTrip() != null
                        ? notification.getTrip().getId()
                        : null,
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}