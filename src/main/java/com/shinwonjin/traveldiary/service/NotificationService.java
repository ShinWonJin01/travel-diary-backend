package com.shinwonjin.traveldiary.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shinwonjin.traveldiary.dto.trip.NotificationResponse;
import com.shinwonjin.traveldiary.entity.Member;
import com.shinwonjin.traveldiary.entity.Notification;
import com.shinwonjin.traveldiary.entity.NotificationType;
import com.shinwonjin.traveldiary.entity.Trip;
import com.shinwonjin.traveldiary.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(
            Member receiver,
            Member actor,
            Trip trip,
            NotificationType type,
            String message
    ) {
        Notification notification = Notification.create(
                receiver,
                actor,
                trip,
                type,
                message
        );

        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long memberId) {
        return notificationRepository
                .findAllByReceiverIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long memberId) {
        return notificationRepository
                .countByReceiverIdAndReadFalse(memberId);
    }

    @Transactional
    public void markAsRead(
            Long memberId,
            Long notificationId
    ) {
        Notification notification = notificationRepository
                .findByIdAndReceiverId(notificationId, memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "알림 정보를 찾을 수 없습니다."
                        )
                );

        notification.markAsRead();
    }

    @Transactional
    public void clearTrip(Long tripId) {
        notificationRepository.clearTripByTripId(tripId);
    }

    @Transactional
    public void cleanupMemberNotifications(Long memberId) {
        notificationRepository.deleteAllByReceiverId(memberId);
        notificationRepository.clearActorByMemberId(memberId);
    }

    @Transactional
    public int markAllAsRead(Long memberId) {
        return notificationRepository.markAllAsReadByReceiverId(memberId);
    }
}