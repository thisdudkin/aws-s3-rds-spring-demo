package com.example.imagestorage.exception;

import java.util.UUID;

public class ImageNotFoundException extends RuntimeException {

    public ImageNotFoundException(UUID id) {
        super("Image metadata not found: " + id);
    }
}

