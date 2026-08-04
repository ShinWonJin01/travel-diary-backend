package com.shinwonjin.traveldiary.dto.trip;

import com.shinwonjin.traveldiary.entity.Member;
import com.shinwonjin.traveldiary.entity.TripPhoto;

import java.time.LocalDateTime;

public record TripPhotoResponse(
        Long id,
        Long tripId,
        Long uploadedByMemberId,
        String uploadedByNickname,
        String filePath,
        String originalFileName,
        LocalDateTime takenAt,
        Double latitude,
        Double longitude,
        LocalDateTime createdAt
) {
    public static TripPhotoResponse from(TripPhoto tripPhoto) {
        Member uploadedBy = tripPhoto.getUploadedBy();

        return new TripPhotoResponse(
                tripPhoto.getId(),
                tripPhoto.getTrip().getId(),
                uploadedBy == null ? null : uploadedBy.getId(),
                uploadedBy == null ? null : uploadedBy.getNickname(),
                tripPhoto.getFilePath(),
                tripPhoto.getOriginalFileName(),
                tripPhoto.getTakenAt(),
                tripPhoto.getLatitude(),
                tripPhoto.getLongitude(),
                tripPhoto.getCreatedAt()
        );
    }
}