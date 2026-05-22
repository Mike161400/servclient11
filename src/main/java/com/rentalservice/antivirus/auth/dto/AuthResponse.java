package com.rentalservice.antivirus.auth.dto;

import java.util.List;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long accessTokenExpiresInSeconds;
    private long refreshTokenExpiresInSeconds;
    private String username;
    private List<String> roles;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken,
                        String refreshToken,
                        String tokenType,
                        long accessTokenExpiresInSeconds,
                        long refreshTokenExpiresInSeconds,
                        String username,
                        List<String> roles) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.accessTokenExpiresInSeconds = accessTokenExpiresInSeconds;
        this.refreshTokenExpiresInSeconds = refreshTokenExpiresInSeconds;
        this.username = username;
        this.roles = roles;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getAccessTokenExpiresInSeconds() {
        return accessTokenExpiresInSeconds;
    }

    public void setAccessTokenExpiresInSeconds(long accessTokenExpiresInSeconds) {
        this.accessTokenExpiresInSeconds = accessTokenExpiresInSeconds;
    }

    public long getRefreshTokenExpiresInSeconds() {
        return refreshTokenExpiresInSeconds;
    }

    public void setRefreshTokenExpiresInSeconds(long refreshTokenExpiresInSeconds) {
        this.refreshTokenExpiresInSeconds = refreshTokenExpiresInSeconds;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
