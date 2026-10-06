package com.bookmyshow.user.controller;

import com.bookmyshow.common.response.ApiResponse;
import com.bookmyshow.user.dto.UserDtos;
import com.bookmyshow.user.entity.User;
import com.bookmyshow.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDtos.AuthResponse>> register(
            @Valid @RequestBody UserDtos.RegisterRequest request,
            HttpServletRequest httpRequest) {
        
        UserDtos.AuthResponse response = userService.register(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDtos.AuthResponse>> login(
            @Valid @RequestBody UserDtos.LoginRequest request,
            HttpServletRequest httpRequest) {
        
        UserDtos.AuthResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<UserDtos.AuthResponse>> refreshToken(
            @Valid @RequestBody UserDtos.RefreshTokenRequest request) {
        
        UserDtos.AuthResponse response = userService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
class UserController {
    
    private final UserService userService;
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDtos.UserInfo>> getProfile(
            @AuthenticationPrincipal User user) {
        
        UserDtos.UserInfo profile = userService.getProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
    
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserDtos.UserInfo>> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserDtos.UpdateProfileRequest request) {
        
        UserDtos.UserInfo profile = userService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
    
    @PostMapping("/me/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserDtos.ChangePasswordRequest request) {
        
        userService.changePassword(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDtos.UserInfo>> getUserById(
            @PathVariable UUID id) {
        
        UserDtos.UserInfo profile = userService.getProfile(id);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
    
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(
            @AuthenticationPrincipal User user) {
        
        // Implementation would set status to DELETED
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}