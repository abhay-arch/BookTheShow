package com.bookmyshow.common.exception;

import com.bookmyshow.common.response.ApiResponse;
import com.bookmyshow.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        String requestId = getRequestId(request);
        log.warn("Business exception: {} - RequestId: {}", ex.getErrorCode().getCode(), requestId);
        
        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getErrorCode().getCode())
                .message(ex.getMessage())
                .requestId(requestId)
                .details(List.of())
                .build();
        
        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiResponse.<ErrorResponse>builder()
                        .data(error)
                        .build());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String requestId = getRequestId(request);
        log.warn("Validation failed - RequestId: {}", requestId);
        
        List<ErrorResponse.ErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::mapFieldError)
                .collect(Collectors.toList());
        
        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.VALIDATION_FAILED.getCode())
                .message(ErrorCode.VALIDATION_FAILED.getDefaultMessage())
                .requestId(requestId)
                .details(details)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<ErrorResponse>builder()
                        .data(error)
                        .build());
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        String requestId = getRequestId(request);
        log.warn("Constraint violation - RequestId: {}", requestId);
        
        List<ErrorResponse.ErrorDetail> details = ex.getConstraintViolations().stream()
                .map(this::mapConstraintViolation)
                .collect(Collectors.toList());
        
        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.VALIDATION_FAILED.getCode())
                .message(ErrorCode.VALIDATION_FAILED.getDefaultMessage())
                .requestId(requestId)
                .details(details)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<ErrorResponse>builder()
                        .data(error)
                        .build());
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        String requestId = getRequestId(request);
        log.warn("Invalid request body - RequestId: {}", requestId);
        
        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.INVALID_REQUEST.getCode())
                .message("Invalid request body: " + ex.getMostSpecificCause().getMessage())
                .requestId(requestId)
                .details(List.of())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<ErrorResponse>builder()
                        .data(error)
                        .build());
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        String requestId = getRequestId(request);
        log.warn("Missing parameter: {} - RequestId: {}", ex.getParameterName(), requestId);
        
        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.INVALID_REQUEST.getCode())
                .message("Missing required parameter: " + ex.getParameterName())
                .requestId(requestId)
                .details(List.of())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<ErrorResponse>builder()
                        .data(error)
                        .build());
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String requestId = getRequestId(request);
        log.warn("Type mismatch for parameter: {} - RequestId: {}", ex.getName(), requestId);
        
        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.INVALID_REQUEST.getCode())
                .message("Invalid type for parameter '" + ex.getName() + "': expected " + 
                        (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"))
                .requestId(requestId)
                .details(List.of())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<ErrorResponse>builder()
                        .data(error)
                        .build());
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        String requestId = getRequestId(request);
        log.warn("Bad credentials - RequestId: {}", requestId);
        
        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.INVALID_CREDENTIALS.getCode())
                .message(ErrorCode.INVALID_CREDENTIALS.getDefaultMessage())
                .requestId(requestId)
                .details(List.of())
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<ErrorResponse>builder()
                        .data(error)
                        .build());
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        String requestId = getRequestId(request);
        log.warn("Access denied - RequestId: {}", requestId);
        
        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.ACCESS_DENIED.getCode())
                .message(ErrorCode.ACCESS_DENIED.getDefaultMessage())
                .requestId(requestId)
                .details(List.of())
                .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.<ErrorResponse>builder()
                        .data(error)
                        .build());
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        String requestId = getRequestId(request);
        log.error("Data integrity violation - RequestId: {}", requestId, ex);
        
        String message = ex.getMostSpecificCause().getMessage();
        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        
        if (message != null) {
            if (message.contains("duplicate key") || message.contains("unique constraint")) {
                if (message.contains("email")) {
                    errorCode = ErrorCode.DUPLICATE_EMAIL;
                } else if (message.contains("phone")) {
                    errorCode = ErrorCode.DUPLICATE_PHONE;
                } else if (message.contains("show_id") && message.contains("seat_id")) {
                    errorCode = ErrorCode.SEAT_ALREADY_BOOKED;
                }
            }
        }
        
        ErrorResponse error = ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getDefaultMessage())
                .requestId(requestId)
                .details(List.of())
                .build();
        
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.<ErrorResponse>builder()
                        .data(error)
                        .build());
    }
    
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        String requestId = getRequestId(request);
        log.warn("Resource not found: {} - RequestId: {}", ex.getResourcePath(), requestId);
        
        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.INVALID_REQUEST.getCode())
                .message("Endpoint not found: " + ex.getResourcePath())
                .requestId(requestId)
                .details(List.of())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.<ErrorResponse>builder()
                        .data(error)
                        .build());
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        String requestId = getRequestId(request);
        log.error("Unexpected error - RequestId: {}", requestId, ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.INTERNAL_ERROR.getCode())
                .message(ErrorCode.INTERNAL_ERROR.getDefaultMessage())
                .requestId(requestId)
                .details(List.of())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<ErrorResponse>builder()
                        .data(error)
                        .build());
    }
    
    private ErrorResponse.ErrorDetail mapFieldError(FieldError fieldError) {
        return ErrorResponse.ErrorDetail.builder()
                .field(fieldError.getField())
                .code(fieldError.getCode())
                .message(fieldError.getDefaultMessage())
                .rejectedValue(fieldError.getRejectedValue())
                .build();
    }
    
    private ErrorResponse.ErrorDetail mapConstraintViolation(ConstraintViolation<?> violation) {
        return ErrorResponse.ErrorDetail.builder()
                .field(violation.getPropertyPath().toString())
                .code(violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName())
                .message(violation.getMessage())
                .rejectedValue(violation.getInvalidValue())
                .build();
    }
    
    private String getRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null) {
            requestId = request.getHeader("X-Correlation-ID");
        }
        if (requestId == null) {
            requestId = java.util.UUID.randomUUID().toString();
        }
        return requestId;
    }
}