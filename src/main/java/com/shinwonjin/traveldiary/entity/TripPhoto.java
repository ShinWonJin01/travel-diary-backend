package com.shinwonjin.traveldiary.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "trip_photos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TripPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_member_id")
    private Member uploadedBy;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private TripPhoto(
            Trip trip,
            Member uploadedBy,
            String filePath,
            String originalFileName,
            LocalDateTime takenAt,
            Double latitude,
            Double longitude
    ) {
        this.trip = trip;
        this.uploadedBy = uploadedBy;
        this.filePath = filePath;
        this.originalFileName = originalFileName;
        this.takenAt = takenAt;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = LocalDateTime.now();
    }

    public static TripPhoto create(
            Trip trip,
            Member uploadedBy,
            String filePath,
            String originalFileName,
            LocalDateTime takenAt,
            Double latitude,
            Double longitude
    ) {
        return new TripPhoto(
                trip,
                uploadedBy,
                filePath,
                originalFileName,
                takenAt,
                latitude,
                longitude
        );
    }
}