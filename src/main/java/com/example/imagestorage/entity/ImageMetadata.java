package com.example.imagestorage.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "image_metadata")
public class ImageMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long fileSize;

    @Column(nullable = false)
    private String bucketName;

    @Column(nullable = false, unique = true)
    private String objectKey;

    @Column(nullable = false)
    private Instant uploadedAt;

    protected ImageMetadata() {
    }

    public ImageMetadata(
            String originalFileName,
            String contentType,
            long fileSize,
            String bucketName,
            String objectKey,
            Instant uploadedAt) {
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.uploadedAt = uploadedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }
}

