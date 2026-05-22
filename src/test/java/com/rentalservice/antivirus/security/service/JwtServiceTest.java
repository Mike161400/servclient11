package com.rentalservice.antivirus.security.service;

import com.rentalservice.antivirus.auth.entity.Role;
import com.rentalservice.antivirus.auth.entity.RoleName;
import com.rentalservice.antivirus.auth.entity.User;
import com.rentalservice.antivirus.security.properties.JwtProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    @Test
    void shouldGenerateAndValidateAccessAndRefreshTokens() {
        JwtProperties properties = new JwtProperties();
        properties.setAccessSecret("01234567890123456789012345678901");
        properties.setRefreshSecret("abcdefghijklmnopqrstuvwxyz123456");
        properties.setAccessExpiration(Duration.ofMinutes(15));
        properties.setRefreshExpiration(Duration.ofDays(30));
        properties.setIssuer("test-antivirus");

        JwtService jwtService = new JwtService(properties);

        Role role = new Role();
        role.setName(RoleName.USER);

        User user = new User();
        user.setUsername("alice");
        user.setPasswordHash("hash");
        user.setEnabled(true);
        user.setRoles(Set.of(role));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        assertTrue(jwtService.validateToken(accessToken));
        assertFalse(jwtService.validateToken(refreshToken));
        assertTrue(jwtService.validateRefreshToken(refreshToken));
        assertEquals("alice", jwtService.extractUsername(accessToken));
        assertEquals("alice", jwtService.extractUsernameFromRefreshToken(refreshToken));
        assertEquals(Set.of("USER"), Set.copyOf(jwtService.extractRoles(accessToken)));
    }
}
