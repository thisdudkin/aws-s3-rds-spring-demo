package com.example.imagestorage.exception;

import org.springframework.http.HttpStatus;

public class InvalidImageException extends RuntimeException {

    private final HttpStatus status;

    public InvalidImageException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

