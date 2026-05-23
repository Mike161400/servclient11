package com.rentalservice.antivirus.auth.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class Ticket {

    private LocalDateTime serverDate;
    private long ticketLifetime;
    private LocalDateTime activationDate;
    private LocalDateTime expirationDate;
    private UUID userId;
    private String deviceId;
    private boolean blocked;

    public LocalDateTime getServerDate() {
        return serverDate;
    }

    public void setServerDate(LocalDateTime serverDate) {
        this.serverDate = serverDate;
    }

    public long getTicketLifetime() {
        return ticketLifetime;
    }

    public void setTicketLifetime(long ticketLifetime) {
        this.ticketLifetime = ticketLifetime;
    }

    public LocalDateTime getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(LocalDateTime activationDate) {
        this.activationDate = activationDate;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}