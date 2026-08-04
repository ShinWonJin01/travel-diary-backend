package com.shinwonjin.traveldiary.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.shinwonjin.traveldiary.dto.trip.TripCreateRequest;
import com.shinwonjin.traveldiary.dto.trip.TripListResponse;
import com.shinwonjin.traveldiary.dto.trip.TripParticipantResponse;
import com.shinwonjin.traveldiary.dto.trip.TripPhotoResponse;
import com.shinwonjin.traveldiary.dto.trip.TripResponse;
import com.shinwonjin.traveldiary.dto.trip.TripSummaryResponse;
import com.shinwonjin.traveldiary.dto.trip.TripUpdateRequest;
import com.shinwonjin.traveldiary.service.TripService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<TripResponse> createTrip(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody TripCreateRequest request
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        TripResponse response =
                tripService.createTrip(memberId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<TripListResponse>> getMyTrips(
            @AuthenticationPrincipal Jwt jwt
    ) {
    Long memberId = Long.valueOf(jwt.getSubject());

    List<TripListResponse> response =
            tripService.getMyTrips(memberId);

    return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<TripSummaryResponse> getTripSummary(
            @AuthenticationPrincipal Jwt jwt
    ) {
    Long memberId = Long.valueOf(jwt.getSubject());

    TripSummaryResponse response =
            tripService.getTripSummary(memberId);

    return ResponseEntity.ok(response);
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponse> getTrip(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        TripResponse response =
                tripService.getTrip(memberId, tripId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{tripId}/members")
    public ResponseEntity<List<TripParticipantResponse>> getTripParticipants(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        List<TripParticipantResponse> response =
                tripService.getTripParticipants(
                        memberId,
                        tripId
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{tripId}")
    public ResponseEntity<TripResponse> updateTrip(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId,
            @Valid @RequestBody TripUpdateRequest request
    ) {
    Long memberId = Long.valueOf(jwt.getSubject());

    TripResponse response =
            tripService.updateTrip(
                    memberId,
                    tripId,
                    request
            );

    return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<Void> deleteTrip(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        tripService.deleteTrip(memberId, tripId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{tripId}/members/me")
    public ResponseEntity<Void> leaveTrip(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        tripService.leaveTrip(memberId, tripId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping(
            value = "/{tripId}/cover-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<TripResponse> uploadCoverImage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId,
            @RequestPart("file") MultipartFile file
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        TripResponse response =
                tripService.uploadCoverImage(
                        memberId,
                        tripId,
                        file
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/{tripId}/photos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<TripPhotoResponse> uploadTripPhoto(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId,
            @RequestPart("file") MultipartFile file
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        TripPhotoResponse response =
                tripService.uploadTripPhoto(
                        memberId,
                        tripId,
                        file
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{tripId}/photos")
    public ResponseEntity<List<TripPhotoResponse>> getTripPhotos(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        List<TripPhotoResponse> response =
                tripService.getTripPhotos(
                        memberId,
                        tripId
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{tripId}/photos/{photoId}")
    public ResponseEntity<Void> deleteTripPhoto(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId,
            @PathVariable Long photoId
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        tripService.deleteTripPhoto(
                memberId,
                tripId,
                photoId
        );

        return ResponseEntity.noContent().build();
    }
}