package com.rentalservice.antivirus.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {

    private String accessSecret;
    private String refreshSecret;
    private Duration accessExpiration;
    private Duration refreshExpiration;
    private String issuer;

    public String getAccessSecret() {
        return accessSecret;
    }

    public void setAccessSecret(String accessSecret) {
        this.accessSecret = accessSecret;
    }

    public String getRefreshSecret() {
        return refreshSecret;
    }

    public void setRefreshSecret(String refreshSecret) {
        this.refreshSecret = refreshSecret;
    }

    public Duration getAccessExpiration() {
        return accessExpiration;
    }

    public void setAccessExpiration(Duration accessExpiration) {
        this.accessExpiration = accessExpiration;
    }

    public Duration getRefreshExpiration() {
        return refreshExpiration;
    }

    public void setRefreshExpiration(Duration refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
