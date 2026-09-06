package com.example.ecommerce.controller;

import com.example.ecommerce.dto.LoginRequest;
import com.example.ecommerce.dto.RegisterRequest;
import com.example.ecommerce.entity.RefreshToken;
import com.example.ecommerce.security.JwtTokenProvider;
import com.example.ecommerce.service.RefreshTokenService;
import com.example.ecommerce.service.AuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        
        String accessToken = tokenProvider.generateToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(request.email());
        
        return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@RequestBody TokenRefreshRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        
        String username = refreshToken.getUser().getEmail();
        // Create new access token without password
        String newAccessToken = tokenProvider.generateToken(
            new UsernamePasswordAuthenticationToken(username, null, null)
        );
        // For simplicity we keep same refresh token, you can rotate if you want
        return ResponseEntity.ok(new JwtResponse(newAccessToken, request.getRefreshToken()));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody TokenRefreshRequest request){
    	refreshTokenService.deleteByToken(request.getRefreshToken());
    	return ResponseEntity.ok("Logged out - refresh token deleted. Access token will expire in 15 min");
    }
    
    @Data
    static class JwtResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        public JwtResponse(String accessToken, String refreshToken) {
            this.accessToken = accessToken; this.refreshToken = refreshToken;
        }
    }
    @Data
    static class TokenRefreshRequest { private String refreshToken; }
}