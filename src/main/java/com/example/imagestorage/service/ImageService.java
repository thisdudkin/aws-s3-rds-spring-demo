package com.example.imagestorage.service;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.imagestorage.config.StorageProperties;
import com.example.imagestorage.dto.ImageMetadataResponse;
import com.example.imagestorage.entity.ImageMetadata;
import com.example.imagestorage.exception.ImageNotFoundException;
import com.example.imagestorage.exception.InvalidImageException;
import com.example.imagestorage.exception.MetadataPersistenceException;
import com.example.imagestorage.exception.StorageOperationException;
import com.example.imagestorage.repository.ImageMetadataRepository;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class ImageService {

    private static final Set<String> SUPPORTED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp");
    private static final Map<String, String> FILE_EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif",
            "image/webp", ".webp");

    private final ImageMetadataRepository repository;
    private final S3Client s3Client;
    private final StorageProperties storageProperties;

    public ImageService(
            ImageMetadataRepository repository,
            S3Client s3Client,
            StorageProperties storageProperties) {
        this.repository = repository;
        this.s3Client = s3Client;
        this.storageProperties = storageProperties;
    }

    public ImageMetadataResponse upload(MultipartFile file) {
        validate(file);

        String originalFileName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        byte[] content = readAndValidateContent(file);
        String objectKey = "images/" + UUID.randomUUID() + FILE_EXTENSIONS.get(file.getContentType());

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(storageProperties.bucket())
                            .key(objectKey)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .build(),
                    RequestBody.fromBytes(content));
        } catch (SdkException exception) {
            throw new StorageOperationException("Could not upload image to object storage", exception);
        }

        var metadata = new ImageMetadata(
                originalFileName,
                file.getContentType(),
                file.getSize(),
                storageProperties.bucket(),
                objectKey,
                Instant.now());

        try {
            return ImageMetadataResponse.from(repository.save(metadata));
        } catch (DataAccessException exception) {
            deleteAfterFailedPersistence(objectKey);
            throw new MetadataPersistenceException("Image uploaded, but metadata could not be saved", exception);
        }
    }

    public List<ImageMetadataResponse> findAll() {
        return repository.findAllByOrderByUploadedAtDesc()
                .stream()
                .map(ImageMetadataResponse::from)
                .toList();
    }

    public ImageMetadataResponse findById(UUID id) {
        return repository.findById(id)
                .map(ImageMetadataResponse::from)
                .orElseThrow(() -> new ImageNotFoundException(id));
    }

    private void validate(MultipartFile file) {
        if (file == null) {
            throw new InvalidImageException(HttpStatus.BAD_REQUEST, "Multipart field 'file' is required");
        }
        if (file.isEmpty()) {
            throw new InvalidImageException(HttpStatus.BAD_REQUEST, "Uploaded file must not be empty");
        }
        if (file.getSize() > storageProperties.maxFileSizeBytes()) {
            throw new InvalidImageException(HttpStatus.CONTENT_TOO_LARGE, "Image exceeds the 5 MB limit");
        }
        if (!SUPPORTED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new InvalidImageException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Supported image types are JPEG, PNG, GIF, and WebP");
        }
    }

    private byte[] readAndValidateContent(MultipartFile file) {
        try {
            byte[] content = file.getBytes();
            if (!hasExpectedSignature(file.getContentType(), content)) {
                throw new InvalidImageException(
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        "File content does not match its image content type");
            }
            return content;
        } catch (IOException exception) {
            throw new InvalidImageException(HttpStatus.BAD_REQUEST, "Could not read uploaded file");
        }
    }

    private boolean hasExpectedSignature(String contentType, byte[] content) {
        return switch (contentType) {
            case "image/jpeg" -> startsWith(content, 0xFF, 0xD8, 0xFF);
            case "image/png" -> startsWith(content, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
            case "image/gif" -> startsWith(content, "GIF87a".getBytes())
                    || startsWith(content, "GIF89a".getBytes());
            case "image/webp" -> startsWith(content, "RIFF".getBytes())
                    && content.length >= 12
                    && Arrays.equals(Arrays.copyOfRange(content, 8, 12), "WEBP".getBytes());
            default -> false;
        };
    }

    private boolean startsWith(byte[] content, int... signature) {
        if (content.length < signature.length) {
            return false;
        }
        for (int index = 0; index < signature.length; index++) {
            if ((content[index] & 0xFF) != signature[index]) {
                return false;
            }
        }
        return true;
    }

    private boolean startsWith(byte[] content, byte[] signature) {
        return content.length >= signature.length
                && Arrays.equals(Arrays.copyOf(content, signature.length), signature);
    }

    private void deleteAfterFailedPersistence(String objectKey) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(storageProperties.bucket())
                    .key(objectKey)
                    .build());
        } catch (SdkException ignored) {
            // Preserve the database error; the object may require manual cleanup.
        }
    }
}
