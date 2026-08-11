package com.shinwonjin.traveldiary.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;

@Service
public class PhotoMetadataService {

    private static final DateTimeFormatter EXIF_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

    public record PhotoMetadata(
            LocalDateTime takenAt,
            Double latitude,
            Double longitude
    ) {}

    public PhotoMetadata extractMetadata(
            MultipartFile file
    ) {
        try (InputStream inputStream = file.getInputStream()) {
            Metadata metadata =
                    ImageMetadataReader.readMetadata(inputStream);

            LocalDateTime takenAt = extractTakenAt(metadata);

            Double latitude = null;
            Double longitude = null;

            GpsDirectory gpsDirectory =
                    metadata.getFirstDirectoryOfType(
                            GpsDirectory.class
                    );

            if (gpsDirectory != null) {
                GeoLocation geoLocation = gpsDirectory.getGeoLocation();

                if (
                        geoLocation != null
                        && !geoLocation.isZero()
                ) {
                    latitude = geoLocation.getLatitude();
                    longitude = geoLocation.getLongitude();
                }
            }

            return new PhotoMetadata(
                    takenAt,
                    latitude,
                    longitude
            );

        } catch (
                IOException
                | ImageProcessingException exception
        ) {
            return new PhotoMetadata(
                    null,
                    null,
                    null
            );
        }
    }

    public LocalDateTime extractTakenAt(
            MultipartFile file
    ) {
        return extractMetadata(file).takenAt();
    }

    private LocalDateTime extractTakenAt(
            Metadata metadata
    ) {
        ExifSubIFDDirectory directory =
                metadata.getFirstDirectoryOfType(
                        ExifSubIFDDirectory.class
                );

        if (directory == null) return null;

        String dateTime =
                directory.getString(
                        ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL
                );

        if (dateTime == null || dateTime.isBlank()) return null;

        try {
            return LocalDateTime.parse(
                    dateTime.trim(),
                    EXIF_DATE_FORMAT
            );
        } catch (DateTimeParseException exception) {
            return null;
        }
    }
}