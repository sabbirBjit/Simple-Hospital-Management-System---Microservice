package com.hms.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private Map<String, String> errors;

    // Constructor for simple error messages without field errors
    public ErrorResponse(String message, int status, LocalDateTime timestamp) {
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
        this.errors = null;
    }
}
