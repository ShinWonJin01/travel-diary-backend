package com.shinwonjin.traveldiary.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 알림을 받는 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_member_id", nullable = false)
    private Member receiver;

    // 알림의 원인이 되는 행동을 한 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_member_id")
    private Member actor;

    // 알림과 관련된 여행
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Notification(
            Member receiver,
            Member actor,
            Trip trip,
            NotificationType type,
            String message
    ) {
        this.receiver = receiver;
        this.actor = actor;
        this.trip = trip;
        this.type = type;
        this.message = message;
    }

    public static Notification create(
            Member receiver,
            Member actor,
            Trip trip,
            NotificationType type,
            String message
    ) {
        return new Notification(
                receiver,
                actor,
                trip,
                type,
                message
        );
    }

    public void markAsRead() {
        this.read = true;
    }

    public void clearActor() {
        this.actor = null;
    }

    public void clearTrip() {
        this.trip = null;
    }
}