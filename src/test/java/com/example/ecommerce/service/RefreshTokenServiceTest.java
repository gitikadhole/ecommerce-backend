package com.example.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.ecommerce.entity.RefreshToken;
import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.RefreshTokenRepository;
import com.example.ecommerce.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 60000L);
    }

    @Test
    void createRefreshToken_shouldPersistTokenForUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("refresh@example.com");
        user.setRole(Role.CUSTOMER);

        when(userRepository.findByEmail("refresh@example.com")).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken token = refreshTokenService.createRefreshToken("refresh@example.com");

        assertNotNull(token);
        assertEquals("refresh@example.com", token.getUser().getEmail());
        assertNotNull(token.getToken());
        assertNotNull(token.getExpiryDate());
    }

    @Test
    void verifyExpiration_shouldThrowWhenTokenIsExpired() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(Instant.now().minusSeconds(30));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> refreshTokenService.verifyExpiration(token));

        assertEquals("Refresh Token Expired.Please login again", ex.getMessage());
    }
}
