package com.example.imagestorage.dto;

import java.time.Instant;
import java.util.UUID;

import com.example.imagestorage.entity.ImageMetadata;

public record ImageMetadataResponse(
        UUID id,
        String originalFileName,
        String contentType,
        long fileSize,
        String bucketName,
        String objectKey,
        Instant uploadedAt) {

    public static ImageMetadataResponse from(ImageMetadata metadata) {
        return new ImageMetadataResponse(
                metadata.getId(),
                metadata.getOriginalFileName(),
                metadata.getContentType(),
                metadata.getFileSize(),
                metadata.getBucketName(),
                metadata.getObjectKey(),
                metadata.getUploadedAt());
    }
}

