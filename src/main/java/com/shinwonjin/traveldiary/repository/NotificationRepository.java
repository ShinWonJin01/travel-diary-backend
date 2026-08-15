package com.shinwonjin.traveldiary.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shinwonjin.traveldiary.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByReceiverIdOrderByCreatedAtDesc(
            Long receiverMemberId
    );

    long countByReceiverIdAndReadFalse(
            Long receiverMemberId
    );

    Optional<Notification> findByIdAndReceiverId(
            Long notificationId,
            Long receiverMemberId
    );

    void deleteAllByReceiverId(Long receiverMemberId);

    @Modifying
    @Query("""
            update Notification notification
            set notification.trip = null
            where notification.trip.id = :tripId
            """)
    void clearTripByTripId(@Param("tripId") Long tripId);

    @Modifying
    @Query("""
            update Notification notification
            set notification.actor = null
            where notification.actor.id = :memberId
            """)
    void clearActorByMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query("""
            update Notification notification
            set notification.read = true
            where notification.receiver.id = :memberId
            and notification.read = false
            """)
    int markAllAsReadByReceiverId(@Param("memberId") Long memberId);
}