package com.example.imagestorage.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class S3Config {

    @Bean
    S3Client s3Client(StorageProperties properties) {
        var builder = S3Client.builder()
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .region(Region.of(properties.region()))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(properties.pathStyleAccess())
                        .build());

        if (properties.endpoint() != null) {
            builder.endpointOverride(properties.endpoint());
        }

        return builder.build();
    }
}
