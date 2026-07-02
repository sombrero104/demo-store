package com.store.controller;

import com.store.dto.ErrorResponseDto;
import com.store.exception.AiIntegrationException;
import com.store.exception.AccountAlreadyExistsException;
import com.store.exception.OrderAccessDeniedException;
import com.store.exception.OrderLockException;
import com.store.exception.OutOfStockException;
import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleException(IllegalArgumentException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex, request);
    }

    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleException(AccountAlreadyExistsException ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "ACCOUNT_ALREADY_EXISTS", ex, request);
    }

    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<ErrorResponseDto> handleException(OutOfStockException ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "OUT_OF_STOCK", ex, request);
    }

    @ExceptionHandler(OrderAccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleException(OrderAccessDeniedException ex, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "ORDER_ACCESS_DENIED", ex, request);
    }

    @ExceptionHandler(OrderLockException.class)
    public ResponseEntity<ErrorResponseDto> handleException(OrderLockException ex, HttpServletRequest request) {
        return error(HttpStatus.LOCKED, "ORDER_LOCKED", ex, request);
    }

    @ExceptionHandler(AiIntegrationException.class)
    public ResponseEntity<ErrorResponseDto> handleException(AiIntegrationException ex, HttpServletRequest request) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "AI_UNAVAILABLE", ex, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleException(AccessDeniedException ex, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> handleException(AuthenticationException ex, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .findFirst()
                .orElse("Validation failed.");
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleException(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .findFirst()
                .orElse("Validation failed.");
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", ex, request);
    }

    private ResponseEntity<ErrorResponseDto> error(
            HttpStatus status,
            String code,
            Exception ex,
            HttpServletRequest request
    ) {
        String traceId = resolveTraceId(request);
        return ResponseEntity.status(status)
                .body(ErrorResponseDto.of(code, ex.getMessage(), request.getRequestURI(), traceId));
    }

    private ResponseEntity<ErrorResponseDto> error(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request
    ) {
        String traceId = resolveTraceId(request);
        return ResponseEntity.status(status)
                .body(ErrorResponseDto.of(code, message, request.getRequestURI(), traceId));
    }

    private String formatFieldError(FieldError fieldError) {
        String defaultMessage = fieldError.getDefaultMessage();
        if (StringUtils.hasText(defaultMessage)) {
            return defaultMessage;
        }
        return fieldError.getField() + " is invalid.";
    }

    private String resolveTraceId(HttpServletRequest request) {
        String requestTraceId = request.getHeader("X-Trace-Id");
        if (StringUtils.hasText(requestTraceId)) {
            return requestTraceId;
        }
        return UUID.randomUUID().toString();
    }

}
