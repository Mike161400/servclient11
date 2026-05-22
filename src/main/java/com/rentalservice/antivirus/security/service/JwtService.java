package com.rentalservice.antivirus.security.service;

import com.rentalservice.antivirus.auth.entity.RoleName;
import com.rentalservice.antivirus.auth.entity.User;
import com.rentalservice.antivirus.security.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private static final String ROLES_CLAIM = "roles";
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final JwtProperties jwtProperties;
    private final SecretKey accessKey;
    private final SecretKey refreshKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.accessKey = buildKey(jwtProperties.getAccessSecret());
        this.refreshKey = buildKey(jwtProperties.getRefreshSecret());
    }

    public String generateAccessToken(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.getUsername())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtProperties.getAccessExpiration())))
                .claim(ROLES_CLAIM, roles)
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .signWith(accessKey)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.getUsername())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtProperties.getRefreshExpiration())))
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .signWith(refreshKey)
                .compact();
    }

    public boolean validateToken(String token) {
        return validateAccessToken(token);
    }

    public boolean validateAccessToken(String token) {
        return isValid(token, accessKey, ACCESS_TOKEN_TYPE);
    }

    public boolean validateRefreshToken(String token) {
        return isValid(token, refreshKey, REFRESH_TOKEN_TYPE);
    }

    public String extractUsername(String token) {
        return parseAccessToken(token).getPayload().getSubject();
    }

    public String extractUsernameFromRefreshToken(String token) {
        return parseRefreshToken(token).getPayload().getSubject();
    }

    public List<String> extractRoles(String token) {
        Claims claims = parseAccessToken(token).getPayload();
        Object roles = claims.get(ROLES_CLAIM);
        if (roles instanceof Collection<?> collection) {
            return collection.stream()
                    .map(String::valueOf)
                    .toList();
        }
        return List.of();
    }

    public Instant extractRefreshTokenExpiration(String token) {
        return parseRefreshToken(token).getPayload().getExpiration().toInstant();
    }

    public List<RoleName> extractRoleNames(String token) {
        return extractRoles(token).stream()
                .map(RoleName::valueOf)
                .toList();
    }

    private boolean isValid(String token, SecretKey key, String expectedType) {
        try {
            Claims claims = parseClaims(token, key).getPayload();
            return jwtProperties.getIssuer().equals(claims.getIssuer())
                    && expectedType.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Jws<Claims> parseAccessToken(String token) {
        return parseClaims(token, accessKey);
    }

    private Jws<Claims> parseRefreshToken(String token) {
        return parseClaims(token, refreshKey);
    }

    private Jws<Claims> parseClaims(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }

    private SecretKey buildKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
