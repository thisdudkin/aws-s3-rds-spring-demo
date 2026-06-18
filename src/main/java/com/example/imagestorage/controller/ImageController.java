package com.example.imagestorage.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.imagestorage.dto.ImageMetadataResponse;
import com.example.imagestorage.service.ImageService;

@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping
    public ResponseEntity<ImageMetadataResponse> upload(
            @RequestPart(name = "file", required = false) MultipartFile file) {
        ImageMetadataResponse response = imageService.upload(file);
        return ResponseEntity.created(URI.create("/images/" + response.id())).body(response);
    }

    @GetMapping
    public List<ImageMetadataResponse> findAll() {
        return imageService.findAll();
    }

    @GetMapping("/{id}")
    public ImageMetadataResponse findById(@PathVariable UUID id) {
        return imageService.findById(id);
    }
}

