package com.rentalservice.antivirus.auth.service;

import com.rentalservice.antivirus.auth.dto.Ticket;
import com.rentalservice.antivirus.auth.dto.TicketResponse;
import com.rentalservice.antivirus.auth.entity.License;
import com.rentalservice.antivirus.auth.entity.User;
import com.rentalservice.antivirus.auth.repository.LicenseRepository;
import com.rentalservice.antivirus.eds.service.SigningService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final SigningService signingService;

    public LicenseService(LicenseRepository licenseRepository,
                          SigningService signingService) {
        this.licenseRepository = licenseRepository;
        this.signingService = signingService;
    }

    public License createLicense(User user, int daysValid) {

        License license = new License();

        license.setLicenseKey(UUID.randomUUID().toString());
        license.setUser(user);
        license.setExpiresAt(LocalDateTime.now().plusDays(daysValid));
        license.setBlocked(false);

        return licenseRepository.save(license);
    }

    public License activateLicense(String licenseKey, String deviceId) {

        License license = licenseRepository.findByLicenseKey(licenseKey)
                .orElseThrow(() -> new RuntimeException("License not found"));

        if (license.getActivatedAt() != null) {
            throw new RuntimeException("License already activated");
        }

        license.setActivatedAt(LocalDateTime.now());
        license.setDeviceId(deviceId);

        return licenseRepository.save(license);
    }

    public TicketResponse verifyLicense(String licenseKey, String deviceId) {

        License license = licenseRepository.findByLicenseKey(licenseKey)
                .orElseThrow(() -> new RuntimeException("License not found"));

        if (license.isBlocked()) {
            throw new RuntimeException("License blocked");
        }

        if (license.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("License expired");
        }

        if (license.getDeviceId() != null &&
                !license.getDeviceId().equals(deviceId)) {

            throw new RuntimeException("Device mismatch");
        }

        Ticket ticket = new Ticket();

        ticket.setServerDate(LocalDateTime.now());
        ticket.setTicketLifetime(300);
        ticket.setActivationDate(license.getActivatedAt());
        ticket.setExpirationDate(license.getExpiresAt());
        ticket.setUserId(license.getUser().getId());
        ticket.setDeviceId(deviceId);
        ticket.setBlocked(license.isBlocked());

        String signature = signingService.signObject(ticket);

        TicketResponse response = new TicketResponse();
        response.setTicket(ticket);
        response.setSignature(signature);

        return response;
    }

    public License renewLicense(String licenseKey, int extraDays) {

        License license = licenseRepository.findByLicenseKey(licenseKey)
                .orElseThrow(() -> new RuntimeException("License not found"));

        license.setExpiresAt(
                license.getExpiresAt().plusDays(extraDays)
        );

        return licenseRepository.save(license);
    }
}