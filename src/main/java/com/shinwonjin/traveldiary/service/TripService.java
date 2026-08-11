package com.shinwonjin.traveldiary.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.shinwonjin.traveldiary.dto.trip.TripCreateRequest;
import com.shinwonjin.traveldiary.dto.trip.TripListResponse;
import com.shinwonjin.traveldiary.dto.trip.TripParticipantResponse;
import com.shinwonjin.traveldiary.dto.trip.TripPhotoMemoUpdateRequest;
import com.shinwonjin.traveldiary.dto.trip.TripPhotoResponse;
import com.shinwonjin.traveldiary.dto.trip.TripPhotoTakenAtUpdateRequest;
import com.shinwonjin.traveldiary.dto.trip.TripResponse;
import com.shinwonjin.traveldiary.dto.trip.TripSummaryResponse;
import com.shinwonjin.traveldiary.dto.trip.TripUpdateRequest;
import com.shinwonjin.traveldiary.entity.Member;
import com.shinwonjin.traveldiary.entity.Trip;
import com.shinwonjin.traveldiary.entity.TripMember;
import com.shinwonjin.traveldiary.entity.TripMemberRole;
import com.shinwonjin.traveldiary.entity.TripMemberStatus;
import com.shinwonjin.traveldiary.entity.TripPhoto;
import com.shinwonjin.traveldiary.repository.MemberRepository;
import com.shinwonjin.traveldiary.repository.TripMemberRepository;
import com.shinwonjin.traveldiary.repository.TripPhotoRepository;
import com.shinwonjin.traveldiary.repository.TripRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final MemberRepository memberRepository;
    private final TripMemberRepository tripMemberRepository;
    private final TripPhotoRepository tripPhotoRepository;
    private final FileStorageService fileStorageService;
    private final PhotoMetadataService photoMetadataService;
    private final ReverseGeocodingService reverseGeocodingService;

    @Transactional
    public TripResponse createTrip(
            Long memberId,
            TripCreateRequest request
    ) {
        validateTripDates(
                request.startDate(),
                request.endDate()
        );

        Member owner = memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "회원 정보를 찾을 수 없습니다."
                        )
                );

        Trip trip = Trip.create(
                owner,
                request.title().strip(),
                request.destination().strip(),
                request.startDate(),
                request.endDate(),
                request.description() == null
                        ? ""
                        : request.description().strip()
        );

        Trip savedTrip = tripRepository.save(trip);

        // 여행 생성자를 여행 참여자 테이블에 자동 등록
        TripMember ownerTripMember =
                TripMember.createOwner(
                        savedTrip,
                        owner
                );

        tripMemberRepository.save(ownerTripMember);

        return TripResponse.from(savedTrip);
    }

    @Transactional
    public TripResponse updateTrip(
            Long memberId,
            Long tripId,
            TripUpdateRequest request
    ) {
    validateTripDates(
            request.startDate(),
            request.endDate()
    );

    Trip trip = tripRepository
            .findByIdAndOwnerId(
                    tripId,
                    memberId
            )
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "여행 정보를 찾을 수 없습니다."
                    )
            );

    trip.update(
            request.title().strip(),
            request.destination().strip(),
            request.startDate(),
            request.endDate(),
            request.description() == null
                    ? ""
                    : request.description().strip()
    );

    return TripResponse.from(trip);
    }

    @Transactional
    public TripResponse uploadCoverImage(
            Long memberId,
            Long tripId,
            MultipartFile file
    ) {
        // 대표 이미지 변경은 여행 생성자만 가능
        Trip trip = tripRepository
                .findByIdAndOwnerId(
                        tripId,
                        memberId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "여행 정보를 찾을 수 없습니다."
                        )
                );

        String coverImagePath =
                fileStorageService.storeTripCoverImage(
                        tripId,
                        file
                );

        trip.updateCoverImagePath(coverImagePath);

        return TripResponse.from(trip);
    }

    @Transactional
    public TripPhotoResponse uploadTripPhoto(
            Long memberId,
            Long tripId,
            MultipartFile file
    ) {
        Trip trip = tripRepository
                .findById(tripId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "여행 정보를 찾을 수 없습니다."
                        )
                );

        boolean isOwner =
                trip.getOwner()
                        .getId()
                        .equals(memberId);

        boolean isAcceptedMember =
                tripMemberRepository
                        .existsByTripIdAndMemberIdAndStatus(
                                tripId,
                                memberId,
                                TripMemberStatus.ACCEPTED
                        );

        if (!isOwner && !isAcceptedMember) {
            throw new IllegalArgumentException(
                    "이 여행에 사진을 등록할 권한이 없습니다."
            );
        }

        Member uploadedBy = memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "회원 정보를 찾을 수 없습니다."
                        )
                );

        PhotoMetadataService.PhotoMetadata metadata =
                photoMetadataService.extractMetadata(file);

        LocalDateTime takenAt = metadata.takenAt();
        Double latitude = metadata.latitude();
        Double longitude = metadata.longitude();

        String locationName = null;

        if (latitude != null && longitude != null) {
        locationName =
                reverseGeocodingService.getLocationName(
                        latitude,
                        longitude
                );
        }

        String filePath =
                fileStorageService.storeTripPhoto(
                        tripId,
                        file
                );

        String originalFileName =
                file.getOriginalFilename();

        if (
                originalFileName == null
                || originalFileName.isBlank()
        ) {
            originalFileName = "photo";
        }

        TripPhoto tripPhoto = TripPhoto.create(
                trip,
                uploadedBy,
                filePath,
                originalFileName,
                takenAt,
                latitude,
                longitude,
                locationName,
                null
        );

        TripPhoto savedPhoto =
                tripPhotoRepository.save(tripPhoto);

        return TripPhotoResponse.from(savedPhoto);
    }

    @Transactional
    public TripPhotoResponse updateTripPhotoMemo(
            Long memberId,
            Long tripId,
            Long photoId,
            TripPhotoMemoUpdateRequest request
    ) {
    TripPhoto tripPhoto = tripPhotoRepository
            .findById(photoId)
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "사진 정보를 찾을 수 없습니다."
                    )
            );

    if (!tripPhoto.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException(
                    "이 여행의 사진이 아닙니다."
            );
    }

    boolean isOwner =
            tripPhoto.getTrip()
                    .getOwner()
                    .getId()
                    .equals(memberId);

    boolean isUploader =
            tripPhoto.getUploadedBy() != null
            && tripPhoto.getUploadedBy()
                    .getId()
                    .equals(memberId);

    if (!isOwner && !isUploader) {
            throw new IllegalArgumentException(
                    "이 사진의 메모를 수정할 권한이 없습니다."
            );
    }

    tripPhoto.updateMemo(request.memo());

    return TripPhotoResponse.from(tripPhoto);
    }

    @Transactional(readOnly = true)
    public List<TripPhotoResponse> getTripPhotos(
            Long memberId,
            Long tripId
    ) {
        Trip trip = tripRepository
                .findById(tripId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "여행 정보를 찾을 수 없습니다."
                        )
                );

        boolean isOwner =
                trip.getOwner()
                        .getId()
                        .equals(memberId);

        boolean isAcceptedMember =
                tripMemberRepository
                        .existsByTripIdAndMemberIdAndStatus(
                                tripId,
                                memberId,
                                TripMemberStatus.ACCEPTED
                        );

        if (!isOwner && !isAcceptedMember) {
            throw new IllegalArgumentException(
                    "이 여행의 사진을 조회할 권한이 없습니다."
            );
        }

        return tripPhotoRepository
                .findAllByTripIdOrderByCreatedAtAsc(tripId)
                .stream()
                .map(TripPhotoResponse::from)
                .toList();
    }

    @Transactional
    public void deleteTripPhoto(
            Long memberId,
            Long tripId,
            Long photoId
    ) {
        TripPhoto tripPhoto = tripPhotoRepository
                .findById(photoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사진 정보를 찾을 수 없습니다."
                        )
                );

        if (!tripPhoto.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException(
                    "이 여행의 사진이 아닙니다."
            );
        }

        boolean isOwner =
                tripPhoto.getTrip()
                        .getOwner()
                        .getId()
                        .equals(memberId);

        boolean isUploader =
                tripPhoto.getUploadedBy() != null
                && tripPhoto.getUploadedBy()
                        .getId()
                        .equals(memberId);

        if (!isOwner && !isUploader) {
            throw new IllegalArgumentException(
                    "이 사진을 삭제할 권한이 없습니다."
            );
        }

        fileStorageService.deleteTripPhoto(
                tripId,
                tripPhoto.getFilePath()
        );

        tripPhotoRepository.delete(tripPhoto);
    }

    @Transactional
    public TripPhotoResponse updateTripPhotoTakenAt(
            Long memberId,
            Long tripId,
            Long photoId,
            TripPhotoTakenAtUpdateRequest request
    ) {
        TripPhoto tripPhoto = tripPhotoRepository
                .findById(photoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사진 정보를 찾을 수 없습니다."
                        )
                );

        if (!tripPhoto.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException(
                    "이 여행의 사진이 아닙니다."
            );
        }

        boolean isOwner =
                tripPhoto.getTrip()
                        .getOwner()
                        .getId()
                        .equals(memberId);

        boolean isUploader =
                tripPhoto.getUploadedBy() != null
                && tripPhoto.getUploadedBy()
                        .getId()
                        .equals(memberId);

        if (!isOwner && !isUploader) {
            throw new IllegalArgumentException(
                    "이 사진의 촬영시간을 수정할 권한이 없습니다."
            );
        }

        tripPhoto.updateTakenAt(request.takenAt());

        return TripPhotoResponse.from(tripPhoto);
    }

    @Transactional(readOnly = true)
    public List<TripListResponse> getMyTrips(
            Long memberId
    ) {
        List<Trip> ownedTrips =
                tripRepository
                        .findAllByOwnerIdOrderByCreatedAtDesc(
                                memberId
                        );

        List<TripMember> participatingTripMembers =
                tripMemberRepository
                        .findAllByMemberIdAndRoleAndStatusOrderByCreatedAtDesc(
                                memberId,
                                TripMemberRole.MEMBER,
                                TripMemberStatus.ACCEPTED
                        );

        return Stream.concat(
                        ownedTrips
                                .stream()
                                .map(trip ->
                                        TripListResponse.from(
                                                trip,
                                                TripMemberRole.OWNER,
                                                countAcceptedParticipants(
                                                        trip.getId()
                                                )
                                        )
                                ),

                        participatingTripMembers
                                .stream()
                                .map(tripMember ->
                                        TripListResponse.from(
                                                tripMember.getTrip(),
                                                TripMemberRole.MEMBER,
                                                countAcceptedParticipants(
                                                        tripMember
                                                                .getTrip()
                                                                .getId()
                                                )
                                        )
                                )
                )
                .sorted(
                        Comparator.comparing(
                                TripListResponse::createdAt
                        ).reversed()
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public TripSummaryResponse getTripSummary(Long memberId) {
        long ownedCount =
                tripRepository.countByOwnerId(memberId);

        long participatingCount =
                tripMemberRepository
                        .countByMemberIdAndRoleAndStatus(
                                memberId,
                                TripMemberRole.MEMBER,
                                TripMemberStatus.ACCEPTED
                        );

        long totalCount =
                ownedCount + participatingCount;

        return new TripSummaryResponse(
                totalCount,
                ownedCount,
                participatingCount
        );
    }

    @Transactional(readOnly = true)
    public TripResponse getTrip(
            Long memberId,
            Long tripId
    ) {
        Trip trip = tripRepository
                .findById(tripId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "여행 정보를 찾을 수 없습니다."
                        )
                );

        boolean isOwner =
                trip.getOwner()
                        .getId()
                        .equals(memberId);

        boolean isAcceptedMember =
                tripMemberRepository
                        .existsByTripIdAndMemberIdAndStatus(
                                tripId,
                                memberId,
                                TripMemberStatus.ACCEPTED
                        );

        // 여행 생성자도 아니고 수락한 참여자도 아니면 조회 불가
        if (!isOwner && !isAcceptedMember) {
            throw new IllegalArgumentException(
                    "이 여행을 조회할 권한이 없습니다."
            );
        }

        return TripResponse.from(trip);
    }

    @Transactional(readOnly = true)
    public List<TripParticipantResponse> getTripParticipants(
            Long memberId,
            Long tripId
    ) {
        Trip trip = tripRepository
                .findById(tripId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "여행 정보를 찾을 수 없습니다."
                        )
                );

        boolean isOwner =
                trip.getOwner()
                        .getId()
                        .equals(memberId);

        boolean isAcceptedMember =
                tripMemberRepository
                        .existsByTripIdAndMemberIdAndStatus(
                                tripId,
                                memberId,
                                TripMemberStatus.ACCEPTED
                        );

        if (!isOwner && !isAcceptedMember) {
            throw new IllegalArgumentException(
                    "이 여행의 참여자 정보를 조회할 권한이 없습니다."
            );
        }

        return tripMemberRepository
                .findAllByTripIdAndStatusOrderByCreatedAtAsc(
                        tripId,
                        TripMemberStatus.ACCEPTED
                )
                .stream()
                .map(TripParticipantResponse::from)
                .toList();
    }

    @Transactional
    public void deleteTrip(
            Long memberId,
            Long tripId
    ) {
        Trip trip = tripRepository
                .findByIdAndOwnerId(tripId, memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "여행 정보를 찾을 수 없습니다."
                        )
                );

        tripPhotoRepository.deleteAllByTripId(tripId);
        tripMemberRepository.deleteAllByTripId(tripId);
        tripRepository.delete(trip);

        fileStorageService.deleteTripFiles(tripId);
    }

    @Transactional
    public void leaveTrip(
            Long memberId,
            Long tripId
    ) {
        TripMember tripMember = tripMemberRepository
                .findByTripIdAndMemberId(
                        tripId,
                        memberId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "참여 중인 여행을 찾을 수 없습니다."
                        )
                );

        if (tripMember.getRole() == TripMemberRole.OWNER) {
            throw new IllegalArgumentException(
                    "여행 생성자는 여행에서 나갈 수 없습니다."
            );
        }

        if (tripMember.getStatus() != TripMemberStatus.ACCEPTED) {
            throw new IllegalArgumentException(
                    "참여 중인 여행만 나갈 수 있습니다."
            );
        }

        tripMemberRepository.delete(tripMember);
    }

    private void validateTripDates(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate
    ) {
        if (
                endDate != null
                && endDate.isBefore(startDate)
        ) {
            throw new IllegalArgumentException(
                    "종료일은 시작일보다 빠를 수 없습니다."
            );
        }
    }

    private long countAcceptedParticipants(
            Long tripId
    ) {
        return tripMemberRepository
                .countByTripIdAndStatus(
                        tripId,
                        TripMemberStatus.ACCEPTED
                );
    }
}