package com.shinwonjin.traveldiary.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private static final long MAX_FILE_SIZE =
            10L * 1024L * 1024L;

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            );

    private final Path uploadRoot;

    public FileStorageService(
            @Value("${app.upload-dir}")
            String uploadDir
    ) {
        this.uploadRoot = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    public String storeTripCoverImage(
            Long tripId,
            MultipartFile file
    ) {
        validateImage(file);

        String extension =
                getExtension(file.getContentType());

        String storedFileName =
                UUID.randomUUID() + extension;

        Path tripDirectory = uploadRoot
                .resolve("trips")
                .resolve(String.valueOf(tripId))
                .normalize();

        if (!tripDirectory.startsWith(uploadRoot)) {
            throw new IllegalArgumentException(
                    "올바르지 않은 파일 저장 경로입니다."
            );
        }

        Path targetPath = tripDirectory
                .resolve(storedFileName)
                .normalize();

        if (!targetPath.startsWith(tripDirectory)) {
            throw new IllegalArgumentException(
                    "올바르지 않은 파일 저장 경로입니다."
            );
        }

        try {
            Files.createDirectories(tripDirectory);

            try (InputStream inputStream =
                         file.getInputStream()) {

                Files.copy(
                        inputStream,
                        targetPath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "이미지 파일을 저장하지 못했습니다.",
                    exception
            );
        }

        return "/uploads/trips/"
                + tripId
                + "/"
                + storedFileName;
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "대표 이미지를 선택해 주세요."
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "대표 이미지는 10MB 이하로 등록해 주세요."
            );
        }

        String contentType = file.getContentType();

        if (
                contentType == null
                || !ALLOWED_CONTENT_TYPES.contains(
                        contentType.toLowerCase(Locale.ROOT)
                )
        ) {
            throw new IllegalArgumentException(
                    "JPG, PNG 또는 WEBP 이미지만 등록할 수 있습니다."
            );
        }
    }

    private String getExtension(String contentType) {
        if (contentType == null) {
            throw new IllegalArgumentException(
                    "이미지 형식을 확인할 수 없습니다."
            );
        }

        return switch (
                contentType.toLowerCase(Locale.ROOT)
        ) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException(
                    "지원하지 않는 이미지 형식입니다."
            );
        };
    }
}