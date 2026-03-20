package com.makingcleancode.shop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", Map.of(
                        "message", ex.getMessage(),
                        "code", 404,
                        "error", "NOT_FOUND"
                )
        ));
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<?> handleIdempotency(IdempotencyConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "status", Map.of(
                        "message", ex.getMessage(),
                        "code", 409,
                        "error", "IDEMPOTENCY_CONFLICT"
                )
        ));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                "status", Map.of(
                        "message", ex.getMessage(),
                        "code", 422,
                        "error", "BUSINESS_ERROR"
                )
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> Map.of(
                        "field", err.getField(),
                        "message", err.getDefaultMessage() == null ? "invalid" : err.getDefaultMessage()
                ))
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status", Map.of(
                        "message", "Validation error",
                        "code", 400,
                        "error", "BAD_REQUEST"
                ),
                "errors", errors
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", Map.of(
                        "message", ex.getMessage(),
                        "code", 400,
                        "error", "BAD_REQUEST"
                )
        ));
    }
}
