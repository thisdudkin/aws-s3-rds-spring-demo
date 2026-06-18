package com.example.imagestorage.controller;

import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.example.imagestorage.dto.ApiError;
import com.example.imagestorage.exception.ImageNotFoundException;
import com.example.imagestorage.exception.InvalidImageException;
import com.example.imagestorage.exception.MetadataPersistenceException;
import com.example.imagestorage.exception.StorageOperationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidImageException.class)
    ResponseEntity<ApiError> handleInvalidImage(
            InvalidImageException exception,
            HttpServletRequest request) {
        return error(exception.getStatus(), exception.getMessage(), request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiError> handleUploadTooLarge(
            MaxUploadSizeExceededException exception,
            HttpServletRequest request) {
        return error(HttpStatus.CONTENT_TOO_LARGE, "Image exceeds the 5 MB limit", request);
    }

    @ExceptionHandler(ImageNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(
            ImageNotFoundException exception,
            HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(StorageOperationException.class)
    ResponseEntity<ApiError> handleStorageFailure(
            StorageOperationException exception,
            HttpServletRequest request) {
        return error(HttpStatus.BAD_GATEWAY, exception.getMessage(), request);
    }

    @ExceptionHandler({MetadataPersistenceException.class, DataAccessException.class})
    ResponseEntity<ApiError> handleDatabaseFailure(
            RuntimeException exception,
            HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Could not persist or retrieve image metadata", request);
    }

    private ResponseEntity<ApiError> error(
            HttpStatus status,
            String message,
            HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()));
    }
}
