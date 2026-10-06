package com.bookmyshow.user.service;

import com.bookmyshow.common.exception.BusinessException;
import com.bookmyshow.common.exception.ErrorCode;
import com.bookmyshow.user.dto.UserDtos;
import com.bookmyshow.user.entity.User;
import com.bookmyshow.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.bookmyshow.common.security.JwtTokenProvider;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    private UserDtos.RegisterRequest registerRequest;
    private User user;
    
    @BeforeEach
    void setUp() {
        registerRequest = UserDtos.RegisterRequest.builder()
                .email("test@example.com")
                .phone("+919876543210")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();
        
        user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .phone("+919876543210")
                .passwordHash("$2a$12$hashedpassword")
                .firstName("John")
                .lastName("Doe")
                .role(User.Role.USER)
                .status(User.UserStatus.ACTIVE)
                .emailVerified(false)
                .phoneVerified(false)
                .failedLoginAttempts(0)
                .build();
    }
    
    @Test
    void shouldRegisterUserSuccessfully() {
        when(userRepository.existsByEmailIgnoreCase(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(registerRequest.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("$2a$12$hashedpassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtTokenProvider.getExpiration(any())).thenReturn(java.time.Instant.now().plusSeconds(900));
        
        UserDtos.AuthResponse response = userService.register(registerRequest);
        
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUser().getEmail()).isEqualTo(registerRequest.getEmail());
        assertThat(response.getUser().getFirstName()).isEqualTo(registerRequest.getFirstName());
        
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        when(userRepository.existsByEmailIgnoreCase(registerRequest.getEmail())).thenReturn(true);
        
        assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);
    }
    
    @Test
    void shouldThrowExceptionWhenPhoneAlreadyExists() {
        when(userRepository.existsByEmailIgnoreCase(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(registerRequest.getPhone())).thenReturn(true);
        
        assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_PHONE);
    }
    
    @Test
    void shouldLoginSuccessfully() {
        UserDtos.LoginRequest loginRequest = UserDtos.LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();
        
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtTokenProvider.getExpiration(any())).thenReturn(java.time.Instant.now().plusSeconds(900));
        
        UserDtos.AuthResponse response = userService.login(loginRequest);
        
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        verify(userRepository).save(user);
    }
    
    @Test
    void shouldRefreshTokenSuccessfully() {
        UserDtos.RefreshTokenRequest refreshRequest = UserDtos.RefreshTokenRequest.builder()
                .refreshToken("valid-refresh-token")
                .build();
        
        when(jwtTokenProvider.validateToken(refreshRequest.getRefreshToken())).thenReturn(true);
        when(jwtTokenProvider.getTokenType(refreshRequest.getRefreshToken())).thenReturn("refresh");
        when(jwtTokenProvider.getUserIdFromToken(refreshRequest.getRefreshToken())).thenReturn(user.getEmail());
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("new-refresh-token");
        when(jwtTokenProvider.getExpiration(any())).thenReturn(java.time.Instant.now().plusSeconds(900));
        
        UserDtos.AuthResponse response = userService.refreshToken(refreshRequest);
        
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
    }
    
    @Test
    void shouldThrowExceptionWhenRefreshTokenInvalid() {
        UserDtos.RefreshTokenRequest refreshRequest = UserDtos.RefreshTokenRequest.builder()
                .refreshToken("invalid-token")
                .build();
        
        when(jwtTokenProvider.validateToken(refreshRequest.getRefreshToken())).thenReturn(false);
        
        assertThatThrownBy(() -> userService.refreshToken(refreshRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REFRESH_TOKEN_INVALID);
    }
    
    @Test
    void shouldChangePasswordSuccessfully() {
        UUID userId = user.getId();
        UserDtos.ChangePasswordRequest request = UserDtos.ChangePasswordRequest.builder()
                .currentPassword("password123")
                .newPassword("newpassword123")
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("$2a$12$newhashedpassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        userService.changePassword(userId, request);
        
        verify(userRepository).save(user);
        assertThat(user.getPasswordHash()).isEqualTo("$2a$12$newhashedpassword");
    }
    
    @Test
    void shouldThrowExceptionWhenCurrentPasswordIncorrect() {
        UUID userId = user.getId();
        UserDtos.ChangePasswordRequest request = UserDtos.ChangePasswordRequest.builder()
                .currentPassword("wrongpassword")
                .newPassword("newpassword123")
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())).thenReturn(false);
        
        assertThatThrownBy(() -> userService.changePassword(userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
    }
}