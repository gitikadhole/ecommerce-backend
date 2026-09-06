package com.example.ecommerce.service;

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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.ecommerce.dto.AuthResponse;
import com.example.ecommerce.dto.LoginRequest;
import com.example.ecommerce.dto.RegisterRequest;
import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldSaveUserAndReturnResponse() {
        RegisterRequest request = new RegisterRequest("alice@example.com", "password123", "Alice");
        when(userRepo.existsByEmail("alice@example.com")).thenReturn(false);
        when(encoder.encode("password123")).thenReturn("encoded-pass");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });

        AuthResponse response = authService.register(request);

        assertEquals("alice@example.com", response.email());
        assertEquals("Alice", response.fullName());
        assertEquals(Role.CUSTOMER, response.role());
        assertEquals("Registered Successfully", response.message());
        verify(userRepo).save(any(User.class));
    }

    @Test
    void login_shouldReturnAuthResponseWhenCredentialsAreValid() {
        User user = new User();
        user.setEmail("bob@example.com");
        user.setPassword("encoded-pass");
        user.setFullName("Bob");
        user.setRole(Role.CUSTOMER);

        when(userRepo.findByEmail("bob@example.com")).thenReturn(Optional.of(user));
        when(encoder.matches("password123", "encoded-pass")).thenReturn(true);

        AuthResponse response = authService.login(new LoginRequest("bob@example.com", "password123"));

        assertNotNull(response);
        assertEquals("bob@example.com", response.email());
        assertEquals("Bob", response.fullName());
        assertEquals(Role.CUSTOMER, response.role());
        assertEquals("Login Successfully", response.message());
    }
}
