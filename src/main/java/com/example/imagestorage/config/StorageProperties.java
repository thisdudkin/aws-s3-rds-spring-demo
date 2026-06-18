package com.example.imagestorage.config;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.storage")
public record StorageProperties(
        String bucket,
        String region,
        URI endpoint,
        boolean pathStyleAccess,
        long maxFileSizeBytes) {
}

