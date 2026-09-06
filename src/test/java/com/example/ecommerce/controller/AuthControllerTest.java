package com.example.ecommerce.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.example.ecommerce.dto.LoginRequest;
import com.example.ecommerce.dto.RegisterRequest;
import com.example.ecommerce.entity.RefreshToken;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.security.JwtTokenProvider;
import com.example.ecommerce.service.AuthService;
import com.example.ecommerce.service.RefreshTokenService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthController authController;

    @Test
    void register_shouldReturnOkResponse() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "Tester");

        ResponseEntity<String> response = authController.register(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("User registered", response.getBody());
        verify(authService).register(request);
    }

    @Test
    void login_shouldReturnAccessAndRefreshTokens() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        Authentication authentication = new UsernamePasswordAuthenticationToken("test@example.com", "password123");
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("access-123");
        when(refreshTokenService.createRefreshToken("test@example.com")).thenReturn(refreshToken);

        ResponseEntity<AuthController.JwtResponse> response = authController.login(request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("access-123", response.getBody().getAccessToken());
        assertEquals("refresh-123", response.getBody().getRefreshToken());
    }

    @Test
    void refresh_shouldIssueNewAccessToken() {
        AuthController.TokenRefreshRequest request = new AuthController.TokenRefreshRequest();
        request.setRefreshToken("refresh-123");

        User user = new User();
        user.setEmail("test@example.com");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken("refresh-123");

        when(refreshTokenService.findByToken("refresh-123")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);
        when(tokenProvider.generateToken(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn("new-access-token");

        ResponseEntity<AuthController.JwtResponse> response = authController.refresh(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("new-access-token", response.getBody().getAccessToken());
        assertEquals("refresh-123", response.getBody().getRefreshToken());
    }

    @Test
    void logout_shouldDeleteRefreshToken() {
        AuthController.TokenRefreshRequest request = new AuthController.TokenRefreshRequest();
        request.setRefreshToken("refresh-123");

        ResponseEntity<String> response = authController.logout(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Logged out - refresh token deleted. Access token will expire in 15 min", response.getBody());
        verify(refreshTokenService).deleteByToken("refresh-123");
    }
}
