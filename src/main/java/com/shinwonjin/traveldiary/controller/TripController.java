package com.shinwonjin.traveldiary.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaTypeFactory;

import com.shinwonjin.traveldiary.dto.trip.TripAiDiaryResponse;
import com.shinwonjin.traveldiary.dto.trip.TripCreateRequest;
import com.shinwonjin.traveldiary.dto.trip.TripListResponse;
import com.shinwonjin.traveldiary.dto.trip.TripParticipantResponse;
import com.shinwonjin.traveldiary.dto.trip.TripPhotoLocationUpdateRequest;
import com.shinwonjin.traveldiary.dto.trip.TripPhotoMemoUpdateRequest;
import com.shinwonjin.traveldiary.dto.trip.TripPhotoResponse;
import com.shinwonjin.traveldiary.dto.trip.TripPhotoTakenAtUpdateRequest;
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

    @GetMapping("/{tripId}/cover-image/file")
    public ResponseEntity<Resource> getTripCoverImage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId
    ) {
        Long memberId =
                Long.valueOf(jwt.getSubject());

        Resource resource =
                tripService.getTripCoverImage(
                        memberId,
                        tripId
                );

        MediaType mediaType =
                MediaTypeFactory
                        .getMediaType(resource)
                        .orElse(
                                MediaType.APPLICATION_OCTET_STREAM
                        );

        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .body(resource);
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

    @GetMapping("/{tripId}/photos/{photoId}/file")
    public ResponseEntity<Resource> getTripPhotoFile(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId,
            @PathVariable Long photoId
    ) {
        Long memberId =
                Long.valueOf(jwt.getSubject());

        Resource resource =
                tripService.getTripPhotoFile(
                        memberId,
                        tripId,
                        photoId
                );

        MediaType mediaType =
                MediaTypeFactory
                        .getMediaType(resource)
                        .orElse(
                                MediaType.APPLICATION_OCTET_STREAM
                        );

        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .body(resource);
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

    @PatchMapping("/{tripId}/photos/{photoId}/memo")
    public ResponseEntity<TripPhotoResponse> updateTripPhotoMemo(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId,
            @PathVariable Long photoId,
            @Valid @RequestBody TripPhotoMemoUpdateRequest request
    ) {
    Long memberId = Long.valueOf(jwt.getSubject());

    TripPhotoResponse response =
            tripService.updateTripPhotoMemo(
                    memberId,
                    tripId,
                    photoId,
                    request
            );

    return ResponseEntity.ok(response);
    }

    @PatchMapping("/{tripId}/photos/{photoId}/taken-at")
    public ResponseEntity<TripPhotoResponse> updateTripPhotoTakenAt(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId,
            @PathVariable Long photoId,
            @RequestBody TripPhotoTakenAtUpdateRequest request
    ) {
    Long memberId = Long.valueOf(jwt.getSubject());

    TripPhotoResponse response =
            tripService.updateTripPhotoTakenAt(
                    memberId,
                    tripId,
                    photoId,
                    request
            );

    return ResponseEntity.ok(response);
    }

    @PatchMapping("/{tripId}/photos/{photoId}/location")
    public ResponseEntity<TripPhotoResponse> updateTripPhotoLocation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId,
            @PathVariable Long photoId,
            @RequestBody TripPhotoLocationUpdateRequest request
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        TripPhotoResponse response =
                tripService.updateTripPhotoLocation(
                        memberId,
                        tripId,
                        photoId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{tripId}/ai-diary")
    public ResponseEntity<TripAiDiaryResponse> getTripAiDiary(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId
    ) {
    Long memberId = Long.valueOf(jwt.getSubject());

    return tripService.getTripAiDiary(memberId, tripId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/{tripId}/ai-diary")
    public ResponseEntity<TripAiDiaryResponse> generateTripAiDiary(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long tripId
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        TripAiDiaryResponse response =
                tripService.generateTripAiDiary(memberId, tripId);

        return ResponseEntity.ok(response);
    }
}