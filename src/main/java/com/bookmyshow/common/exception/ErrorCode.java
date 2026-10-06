package com.bookmyshow.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Validation
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Request validation failed"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Invalid request payload"),
    
    // Authentication
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", "Authentication failed"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "Access token has expired"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "Access token is invalid"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED", "Refresh token has expired"),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_INVALID", "Refresh token is invalid"),
    
    // Authorization
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied"),
    INSUFFICIENT_PERMISSIONS(HttpStatus.FORBIDDEN, "INSUFFICIENT_PERMISSIONS", "Insufficient permissions"),
    
    // Not Found
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"),
    MOVIE_NOT_FOUND(HttpStatus.NOT_FOUND, "MOVIE_NOT_FOUND", "Movie not found"),
    CITY_NOT_FOUND(HttpStatus.NOT_FOUND, "CITY_NOT_FOUND", "City not found"),
    VENUE_NOT_FOUND(HttpStatus.NOT_FOUND, "VENUE_NOT_FOUND", "Venue not found"),
    SCREEN_NOT_FOUND(HttpStatus.NOT_FOUND, "SCREEN_NOT_FOUND", "Screen not found"),
    SHOW_NOT_FOUND(HttpStatus.NOT_FOUND, "SHOW_NOT_FOUND", "Show not found"),
    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "SEAT_NOT_FOUND", "Seat not found"),
    BOOKING_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND", "Booking not found"),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", "Payment not found"),
    
    // Conflict
    SEAT_ALREADY_HELD(HttpStatus.CONFLICT, "SEAT_ALREADY_HELD", "Selected seat is already held"),
    SEAT_ALREADY_BOOKED(HttpStatus.CONFLICT, "SEAT_ALREADY_BOOKED", "Selected seat is already booked"),
    HOLD_EXPIRED(HttpStatus.CONFLICT, "HOLD_EXPIRED", "Seat hold has expired"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "Email already registered"),
    DUPLICATE_PHONE(HttpStatus.CONFLICT, "DUPLICATE_PHONE", "Phone number already registered"),
    IDEMPOTENCY_KEY_CONFLICT(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_CONFLICT", "Idempotency key already used with different payload"),
    
    // Business Rules
    INVALID_SEAT_SELECTION(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_SEAT_SELECTION", "Invalid seat selection"),
    BOOKING_EXPIRED(HttpStatus.UNPROCESSABLE_ENTITY, "BOOKING_EXPIRED", "Booking has expired"),
    PAYMENT_FAILED(HttpStatus.UNPROCESSABLE_ENTITY, "PAYMENT_FAILED", "Payment failed"),
    PAYMENT_ALREADY_PROCESSED(HttpStatus.UNPROCESSABLE_ENTITY, "PAYMENT_ALREADY_PROCESSED", "Payment already processed"),
    REFUND_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_ENTITY, "REFUND_NOT_ALLOWED", "Refund not allowed for this booking"),
    INSUFFICIENT_SEATS(HttpStatus.UNPROCESSABLE_ENTITY, "INSUFFICIENT_SEATS", "Not enough seats available"),
    MAX_SEATS_EXCEEDED(HttpStatus.UNPROCESSABLE_ENTITY, "MAX_SEATS_EXCEEDED", "Maximum seats per booking exceeded"),
    SHOW_NOT_BOOKABLE(HttpStatus.UNPROCESSABLE_ENTITY, "SHOW_NOT_BOOKABLE", "Show is not available for booking"),
    
    // External
    PAYMENT_PROVIDER_ERROR(HttpStatus.BAD_GATEWAY, "PAYMENT_PROVIDER_ERROR", "Payment provider error"),
    NOTIFICATION_PROVIDER_ERROR(HttpStatus.BAD_GATEWAY, "NOTIFICATION_PROVIDER_ERROR", "Notification provider error"),
    
    // Internal
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred"),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DATABASE_ERROR", "Database error"),
    CACHE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CACHE_ERROR", "Cache error"),
    MESSAGING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "MESSAGING_ERROR", "Messaging error");
    
    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;
    
    ErrorCode(HttpStatus httpStatus, String code, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}