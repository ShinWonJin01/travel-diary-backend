package com.shinwonjin.traveldiary.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.shinwonjin.traveldiary.dto.invitation.TripInvitationRequest;
import com.shinwonjin.traveldiary.dto.invitation.TripInvitationResponse;
import com.shinwonjin.traveldiary.service.TripInvitationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TripInvitationController {

    private final TripInvitationService
            tripInvitationService;

    /**
     * 여행에 회원 초대
     */
    @PostMapping(
            "/api/trips/{tripId}/invitations"
    )
    public ResponseEntity<TripInvitationResponse>
    inviteMember(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId,
            @Valid
            @RequestBody TripInvitationRequest request
    ) {
        Long ownerId =
                Long.valueOf(jwt.getSubject());

        TripInvitationResponse response =
                tripInvitationService.inviteMember(
                        ownerId,
                        tripId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * 내가 받은 초대 목록
     */
    @GetMapping(
            "/api/invitations/received"
    )
    public ResponseEntity<
            List<TripInvitationResponse>
    > getReceivedInvitations(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long memberId =
                Long.valueOf(jwt.getSubject());

        List<TripInvitationResponse> response =
                tripInvitationService
                        .getReceivedInvitations(
                                memberId
                        );

        return ResponseEntity.ok(response);
    }

    /**
     * 내가 보낸 초대 목록
     */
    @GetMapping(
            "/api/invitations/sent"
    )
    public ResponseEntity<
            List<TripInvitationResponse>
    > getSentInvitations(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long memberId =
                Long.valueOf(jwt.getSubject());

        List<TripInvitationResponse> response =
                tripInvitationService
                        .getSentInvitations(
                                memberId
                        );

        return ResponseEntity.ok(response);
    }

    /**
     * 초대 수락
     */
    @PatchMapping(
            "/api/invitations/{invitationId}/accept"
    )
    public ResponseEntity<TripInvitationResponse>
    acceptInvitation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long invitationId
    ) {
        Long memberId =
                Long.valueOf(jwt.getSubject());

        TripInvitationResponse response =
                tripInvitationService
                        .acceptInvitation(
                                memberId,
                                invitationId
                        );

        return ResponseEntity.ok(response);
    }

    /**
     * 초대 거절
     */
    @PatchMapping(
            "/api/invitations/{invitationId}/decline"
    )
    public ResponseEntity<TripInvitationResponse>
    declineInvitation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long invitationId
    ) {
        Long memberId =
                Long.valueOf(jwt.getSubject());

        TripInvitationResponse response =
                tripInvitationService
                        .declineInvitation(
                                memberId,
                                invitationId
                        );

        return ResponseEntity.ok(response);
    }

    /**
     * 초대 취소
     */
    @DeleteMapping(
        "/api/invitations/{invitationId}"
    )
    public ResponseEntity<Void> cancelInvitation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long invitationId
    ) {
        Long ownerId =
                Long.valueOf(jwt.getSubject());

        tripInvitationService.cancelInvitation(
                ownerId,
                invitationId
        );

        return ResponseEntity.noContent().build();
    }
}