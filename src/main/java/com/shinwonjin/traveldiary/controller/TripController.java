package com.shinwonjin.traveldiary.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.shinwonjin.traveldiary.dto.trip.TripCreateRequest;
import com.shinwonjin.traveldiary.dto.trip.TripResponse;
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
    public ResponseEntity<List<TripResponse>> getMyTrips(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());

        List<TripResponse> response =
                tripService.getMyTrips(memberId);

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
}