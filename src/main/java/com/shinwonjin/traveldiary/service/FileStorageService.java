package com.shinwonjin.traveldiary.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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

    private static final String PROFILE_IMAGE_PREFIX =
            "/uploads/profiles/";

    private final Path uploadRoot;

    public FileStorageService(
            @Value("${app.upload-dir}") String uploadDir
    ) {
        this.uploadRoot = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    public String storeTripCoverImage(
            Long tripId,
            MultipartFile file
    ) {
        validateImage(
                file,
                "대표 이미지를 선택해 주세요.",
                "대표 이미지는 10MB 이하로 등록해 주세요."
        );

        return storeImage(
                "trips",
                String.valueOf(tripId),
                file
        );
    }

    public String storeTripPhoto(
            Long tripId,
            MultipartFile file
    ) {
        validateImage(
                file,
                "여행 사진을 선택해 주세요.",
                "여행 사진은 10MB 이하로 등록해 주세요."
        );

        return storeImage(
                "trips",
                String.valueOf(tripId),
                file
        );
    }

    public String storeProfileImage(
            Long memberId,
            MultipartFile file
    ) {
        validateImage(
                file,
                "프로필 이미지를 선택해 주세요.",
                "프로필 이미지는 10MB 이하로 등록해 주세요."
        );

        return storeImage(
                "profiles",
                String.valueOf(memberId),
                file
        );
    }

    public Resource loadTripImage(
            Long tripId,
            String imagePath
    ) {
        if (imagePath == null || imagePath.isBlank()) {
            throw new IllegalArgumentException(
                    "이미지 정보를 찾을 수 없습니다."
            );
        }

        String expectedPrefix =
                "/uploads/trips/"
                        + tripId
                        + "/";

        if (!imagePath.startsWith(expectedPrefix)) {
            throw new IllegalArgumentException(
                    "올바르지 않은 여행 이미지 경로입니다."
            );
        }

        String relativePath =
                imagePath.substring(
                        "/uploads/".length()
                );

        Path tripDirectory = uploadRoot
                .resolve("trips")
                .resolve(String.valueOf(tripId))
                .normalize();

        Path targetPath = uploadRoot
                .resolve(relativePath)
                .normalize();

        if (!targetPath.startsWith(tripDirectory)) {
            throw new IllegalArgumentException(
                    "올바르지 않은 여행 이미지 경로입니다."
            );
        }

        if (
                !Files.exists(targetPath)
                        || !Files.isRegularFile(targetPath)
                        || !Files.isReadable(targetPath)
        ) {
            throw new IllegalArgumentException(
                    "이미지 파일을 찾을 수 없습니다."
            );
        }

        try {
            return new UrlResource(targetPath.toUri());
        } catch (MalformedURLException exception) {
            throw new IllegalStateException(
                    "이미지 파일을 불러오지 못했습니다.",
                    exception
            );
        }
    }

    public Resource loadProfileImage(
            Long memberId,
            String imagePath
    ) {
        if (imagePath == null || imagePath.isBlank()) {
            throw new IllegalArgumentException(
                    "프로필 이미지 정보를 찾을 수 없습니다."
            );
        }

        String expectedPrefix =
                "/uploads/profiles/"
                        + memberId
                        + "/";

        if (!imagePath.startsWith(expectedPrefix)) {
            throw new IllegalArgumentException(
                    "올바르지 않은 프로필 이미지 경로입니다."
            );
        }

        String relativePath =
                imagePath.substring(
                        "/uploads/".length()
                );

        Path profileDirectory = uploadRoot
                .resolve("profiles")
                .resolve(String.valueOf(memberId))
                .normalize();

        Path targetPath = uploadRoot
                .resolve(relativePath)
                .normalize();

        if (!targetPath.startsWith(profileDirectory)) {
            throw new IllegalArgumentException(
                    "올바르지 않은 프로필 이미지 경로입니다."
            );
        }

        if (
                !Files.exists(targetPath)
                        || !Files.isRegularFile(targetPath)
                        || !Files.isReadable(targetPath)
        ) {
            throw new IllegalArgumentException(
                    "프로필 이미지 파일을 찾을 수 없습니다."
            );
        }

        try {
            return new UrlResource(targetPath.toUri());
        } catch (MalformedURLException exception) {
            throw new IllegalStateException(
                    "프로필 이미지 파일을 불러오지 못했습니다.",
                    exception
            );
        }
    }

    public void deleteProfileImage(
            String profileImagePath
    ) {
        if (
                profileImagePath == null
                        || profileImagePath.isBlank()
        ) {
            return;
        }

        if (!profileImagePath.startsWith(PROFILE_IMAGE_PREFIX)) {
            throw new IllegalArgumentException(
                    "올바르지 않은 프로필 이미지 경로입니다."
            );
        }

        String relativePath =
                profileImagePath.substring(
                        "/uploads/".length()
                );

        Path targetPath = uploadRoot
                .resolve(relativePath)
                .normalize();

        if (!targetPath.startsWith(uploadRoot)) {
            throw new IllegalArgumentException(
                    "올바르지 않은 파일 삭제 경로입니다."
            );
        }

        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "프로필 이미지 파일을 삭제하지 못했습니다.",
                    exception
            );
        }
    }

    public void deleteTripPhoto(
            Long tripId,
            String photoPath
    ) {
        if (photoPath == null || photoPath.isBlank()) {
            return;
        }

        String expectedPrefix =
                "/uploads/trips/"
                        + tripId
                        + "/";

        if (!photoPath.startsWith(expectedPrefix)) {
            throw new IllegalArgumentException(
                    "올바르지 않은 여행 사진 경로입니다."
            );
        }

        String relativePath =
                photoPath.substring(
                        "/uploads/".length()
                );

        Path targetPath = uploadRoot
                .resolve(relativePath)
                .normalize();

        if (!targetPath.startsWith(uploadRoot)) {
            throw new IllegalArgumentException(
                    "올바르지 않은 파일 삭제 경로입니다."
            );
        }

        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "여행 사진 파일을 삭제하지 못했습니다.",
                    exception
            );
        }
    }

    public void deleteTripFiles(Long tripId) {
        Path tripDirectory = uploadRoot
                .resolve("trips")
                .resolve(String.valueOf(tripId))
                .normalize();

        if (!tripDirectory.startsWith(uploadRoot)) {
            throw new IllegalArgumentException(
                    "올바르지 않은 여행 파일 삭제 경로입니다."
            );
        }

        if (!Files.exists(tripDirectory)) {
            return;
        }

        try (var paths = Files.walk(tripDirectory)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException exception) {
                            throw new IllegalStateException(
                                    "여행 이미지 파일을 삭제하지 못했습니다.",
                                    exception
                            );
                        }
                    });
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "여행 이미지 폴더를 삭제하지 못했습니다.",
                    exception
            );
        }
    }

    private String storeImage(
            String category,
            String targetId,
            MultipartFile file
    ) {
        String extension =
                getExtension(file.getContentType());

        String storedFileName =
                UUID.randomUUID() + extension;

        Path targetDirectory = uploadRoot
                .resolve(category)
                .resolve(targetId)
                .normalize();

        if (!targetDirectory.startsWith(uploadRoot)) {
            throw new IllegalArgumentException(
                    "올바르지 않은 파일 저장 경로입니다."
            );
        }

        Path targetPath = targetDirectory
                .resolve(storedFileName)
                .normalize();

        if (!targetPath.startsWith(targetDirectory)) {
            throw new IllegalArgumentException(
                    "올바르지 않은 파일 저장 경로입니다."
            );
        }

        try {
            Files.createDirectories(targetDirectory);

            try (InputStream inputStream = file.getInputStream()) {
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

        return "/uploads/"
                + category
                + "/"
                + targetId
                + "/"
                + storedFileName;
    }

    private void validateImage(
            MultipartFile file,
            String emptyMessage,
            String sizeMessage
    ) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    emptyMessage
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    sizeMessage
            );
        }

        String contentType =
                file.getContentType();

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

        validateImageSignature(
                file,
                contentType
        );
    }

    private void validateImageSignature(
            MultipartFile file,
            String contentType
    ) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header =
                    inputStream.readNBytes(12);

            boolean valid = switch (
                    contentType.toLowerCase(Locale.ROOT)
            ) {
                case "image/jpeg" ->
                        header.length >= 3
                                && (header[0] & 0xFF) == 0xFF
                                && (header[1] & 0xFF) == 0xD8
                                && (header[2] & 0xFF) == 0xFF;

                case "image/png" ->
                        header.length >= 8
                                && (header[0] & 0xFF) == 0x89
                                && header[1] == 0x50
                                && header[2] == 0x4E
                                && header[3] == 0x47
                                && header[4] == 0x0D
                                && header[5] == 0x0A
                                && header[6] == 0x1A
                                && header[7] == 0x0A;

                case "image/webp" ->
                        header.length >= 12
                                && header[0] == 'R'
                                && header[1] == 'I'
                                && header[2] == 'F'
                                && header[3] == 'F'
                                && header[8] == 'W'
                                && header[9] == 'E'
                                && header[10] == 'B'
                                && header[11] == 'P';

                default -> false;
            };

            if (!valid) {
                throw new IllegalArgumentException(
                        "실제 이미지 파일 형식이 올바르지 않습니다."
                );
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "이미지 파일을 확인할 수 없습니다.",
                    exception
            );
        }
    }

    private String getExtension(
            String contentType
    ) {
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

            default ->
                    throw new IllegalArgumentException(
                            "지원하지 않는 이미지 형식입니다."
                    );
        };
    }
}