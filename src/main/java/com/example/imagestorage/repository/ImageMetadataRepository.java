package com.example.imagestorage.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.imagestorage.entity.ImageMetadata;

public interface ImageMetadataRepository extends JpaRepository<ImageMetadata, UUID> {

    List<ImageMetadata> findAllByOrderByUploadedAtDesc();
}

