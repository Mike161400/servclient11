package com.rentalservice.antivirus.auth.service;

import com.rentalservice.antivirus.auth.dto.AuthResponse;
import com.rentalservice.antivirus.auth.dto.LoginRequest;
import com.rentalservice.antivirus.auth.dto.RefreshTokenRequest;
import com.rentalservice.antivirus.auth.dto.RegisterRequest;
import com.rentalservice.antivirus.auth.entity.RefreshToken;
import com.rentalservice.antivirus.auth.entity.Role;
import com.rentalservice.antivirus.auth.entity.RoleName;
import com.rentalservice.antivirus.auth.entity.User;
import com.rentalservice.antivirus.auth.exception.InvalidCredentialsException;
import com.rentalservice.antivirus.auth.exception.TokenRefreshException;
import com.rentalservice.antivirus.auth.exception.UserAlreadyExistsException;
import com.rentalservice.antivirus.auth.repository.RefreshTokenRepository;
import com.rentalservice.antivirus.auth.repository.RoleRepository;
import com.rentalservice.antivirus.auth.repository.UserRepository;
import com.rentalservice.antivirus.security.properties.JwtProperties;
import com.rentalservice.antivirus.security.service.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class AuthService {

    private static final String BEARER = "Bearer";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username is already in use");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already in use");
        }

        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new InvalidCredentialsException("Default USER role is missing"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setRoles(Set.of(userRole));

        User savedUser = userRepository.save(user);
        return issueTokens(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!user.isEnabled() || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        return issueTokens(user);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String refreshTokenValue = request.getRefreshToken();

        if (!jwtService.validateRefreshToken(refreshTokenValue)) {
            throw new TokenRefreshException("Refresh token is invalid or expired");
        }

        RefreshToken storedToken = refreshTokenRepository.findByTokenValueAndRevokedFalse(refreshTokenValue)
                .orElseThrow(() -> new TokenRefreshException("Refresh token is not recognized"));

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new TokenRefreshException("Refresh token is expired");
        }

        String username = jwtService.extractUsernameFromRefreshToken(refreshTokenValue);
        if (!storedToken.getUser().getUsername().equals(username)) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new TokenRefreshException("Refresh token subject mismatch");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();
        return issueTokens(user);
    }

    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByTokenValue(request.getRefreshToken())
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    private AuthResponse issueTokens(User user) {
        refreshTokenRepository.deleteByUser_Id(user.getId());

        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = jwtService.generateRefreshToken(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenValue(refreshTokenValue);
        refreshToken.setExpiresAt(LocalDateTime.ofInstant(
                jwtService.extractRefreshTokenExpiration(refreshTokenValue),
                ZoneOffset.UTC
        ));
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .sorted()
                .toList();

        return new AuthResponse(
                accessToken,
                refreshTokenValue,
                BEARER,
                jwtProperties.getAccessExpiration().toSeconds(),
                jwtProperties.getRefreshExpiration().toSeconds(),
                user.getUsername(),
                roles
        );
    }
}
