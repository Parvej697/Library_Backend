package com.library.exception;

import com.library.dto.AuthDto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex) {
        return ResponseEntity.internalServerError().body(
                ApiResponse.builder()
                        .success(false)
                        .message("An error occurred: " + ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(Exception ex) {
        return ResponseEntity.status(403).body(
                ApiResponse.builder()
                        .success(false)
                        .message("Access denied. Admin privileges required.")
                        .build()
        );
    }
}