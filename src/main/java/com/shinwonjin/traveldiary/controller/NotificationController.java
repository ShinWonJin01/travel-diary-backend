package com.shinwonjin.traveldiary.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shinwonjin.traveldiary.dto.trip.NotificationResponse;
import com.shinwonjin.traveldiary.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        List<NotificationResponse> response =
                notificationService.getNotifications(memberId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        long response =
                notificationService.getUnreadCount(memberId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long notificationId
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        notificationService.markAsRead(
                memberId,
                notificationId
        );

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        notificationService.markAllAsRead(memberId);

        return ResponseEntity.noContent().build();
    }
}