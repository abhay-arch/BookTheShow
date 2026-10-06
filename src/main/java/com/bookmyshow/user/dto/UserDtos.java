package com.bookmyshow.user.dto;

import com.bookmyshow.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class UserDtos {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank
        @Email
        @Size(max = 255)
        private String email;
        
        @NotBlank
        @Pattern(regexp = "^[+]?[0-9]{10,15}$")
        private String phone;
        
        @NotBlank
        @Size(min = 8, max = 100)
        private String password;
        
        @NotBlank
        @Size(max = 100)
        private String firstName;
        
        @Size(max = 100)
        private String lastName;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank
        private String email;
        
        @NotBlank
        private String password;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshTokenRequest {
        @NotBlank
        private String refreshToken;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Long expiresIn;
        private UserInfo user;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private UUID id;
        private String email;
        private String phone;
        private String firstName;
        private String lastName;
        private User.Role role;
        private User.UserStatus status;
        private Boolean emailVerified;
        private Boolean phoneVerified;
        private Instant lastLoginAt;
        
        public static UserInfo from(User user) {
            return UserInfo.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getRole())
                    .status(user.getStatus())
                    .emailVerified(user.getEmailVerified())
                    .phoneVerified(user.getPhoneVerified())
                    .lastLoginAt(user.getLastLoginAt())
                    .build();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateProfileRequest {
        @Size(max = 100)
        private String firstName;
        
        @Size(max = 100)
        private String lastName;
        
        @Pattern(regexp = "^[+]?[0-9]{10,15}$")
        private String phone;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordRequest {
        @NotBlank
        private String currentPassword;
        
        @NotBlank
        @Size(min = 8, max = 100)
        private String newPassword;
    }
}