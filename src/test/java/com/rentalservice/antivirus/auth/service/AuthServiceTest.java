package com.rentalservice.antivirus.auth.service;

import com.rentalservice.antivirus.auth.dto.LoginRequest;
import com.rentalservice.antivirus.auth.dto.RefreshTokenRequest;
import com.rentalservice.antivirus.auth.dto.RegisterRequest;
import com.rentalservice.antivirus.auth.entity.RefreshToken;
import com.rentalservice.antivirus.auth.entity.Role;
import com.rentalservice.antivirus.auth.entity.RoleName;
import com.rentalservice.antivirus.auth.entity.User;
import com.rentalservice.antivirus.auth.exception.InvalidCredentialsException;
import com.rentalservice.antivirus.auth.exception.TokenRefreshException;
import com.rentalservice.antivirus.auth.repository.RefreshTokenRepository;
import com.rentalservice.antivirus.auth.repository.RoleRepository;
import com.rentalservice.antivirus.auth.repository.UserRepository;
import com.rentalservice.antivirus.security.properties.JwtProperties;
import com.rentalservice.antivirus.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private JwtProperties jwtProperties;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();

        jwtProperties = new JwtProperties();
        jwtProperties.setAccessSecret("01234567890123456789012345678901");
        jwtProperties.setRefreshSecret("abcdefghijklmnopqrstuvwxyz123456");
        jwtProperties.setAccessExpiration(Duration.ofMinutes(15));
        jwtProperties.setRefreshExpiration(Duration.ofDays(30));
        jwtProperties.setIssuer("test-antivirus");
        jwtService = new JwtService(jwtProperties);

        authService = new AuthService(
                userRepository,
                roleRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtService,
                jwtProperties
        );
    }

    @Test
    void shouldRegisterUserWithBcryptPasswordAndIssueTokens() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");
        request.setEmail("alice@example.com");
        request.setPassword("StrongPass123");

        Role userRole = new Role();
        userRole.setName(RoleName.USER);

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("alice", savedUser.getUsername());
        assertEquals("alice@example.com", savedUser.getEmail());
        assertNotEquals("StrongPass123", savedUser.getPasswordHash());
        assertTrue(passwordEncoder.matches("StrongPass123", savedUser.getPasswordHash()));
        assertEquals("alice", response.getUsername());
        assertEquals(Set.of("USER"), Set.copyOf(response.getRoles()));
        verify(refreshTokenRepository).deleteByUser_Id(any(UUID.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void shouldRejectLoginWithInvalidPassword() {
        User user = buildUser("alice", "alice@example.com", "StrongPass123", RoleName.USER);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("WrongPass123");

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void shouldRotateRefreshToken() {
        User user = buildUser("alice", "alice@example.com", "StrongPass123", RoleName.USER);
        String refreshTokenValue = jwtService.generateRefreshToken(user);

        RefreshToken storedToken = new RefreshToken();
        storedToken.setUser(user);
        storedToken.setTokenValue(refreshTokenValue);
        storedToken.setExpiresAt(LocalDateTime.ofInstant(Instant.now().plus(Duration.ofDays(1)), ZoneOffset.UTC));
        storedToken.setRevoked(false);

        when(refreshTokenRepository.findByTokenValueAndRevokedFalse(refreshTokenValue)).thenReturn(Optional.of(storedToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(refreshTokenValue);

        var response = authService.refresh(request);

        assertEquals("alice", response.getUsername());
        verify(refreshTokenRepository).save(eq(storedToken));
        verify(refreshTokenRepository).deleteByUser_Id(user.getId());
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    void shouldRejectUnknownRefreshToken() {
        User user = buildUser("alice", "alice@example.com", "StrongPass123", RoleName.USER);
        String refreshTokenValue = jwtService.generateRefreshToken(user);

        when(refreshTokenRepository.findByTokenValueAndRevokedFalse(anyString())).thenReturn(Optional.empty());

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(refreshTokenValue);

        assertThrows(TokenRefreshException.class, () -> authService.refresh(request));
    }

    @Test
    void shouldLogoutIdempotently() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("missing-token");

        when(refreshTokenRepository.findByTokenValue("missing-token")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> authService.logout(request));
    }

    private User buildUser(String username, String email, String rawPassword, RoleName roleName) {
        Role role = new Role();
        role.setName(roleName);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setEnabled(true);
        user.setRoles(Set.of(role));
        return user;
    }
}
