package com.rentalservice.antivirus.auth.controller;

import com.rentalservice.antivirus.auth.dto.TicketResponse;
import com.rentalservice.antivirus.auth.entity.License;
import com.rentalservice.antivirus.auth.entity.User;
import com.rentalservice.antivirus.auth.repository.UserRepository;
import com.rentalservice.antivirus.auth.service.LicenseService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/licenses")
public class LicenseController {

    private final LicenseService licenseService;
    private final UserRepository userRepository;

    public LicenseController(LicenseService licenseService,
                             UserRepository userRepository) {
        this.licenseService = licenseService;
        this.userRepository = userRepository;
    }

    @PostMapping("/create")
    public License createLicense(@RequestParam String username,
                                 @RequestParam int daysValid) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return licenseService.createLicense(user, daysValid);
    }

    @PostMapping("/activate")
    public License activateLicense(@RequestParam String licenseKey,
                                   @RequestParam String deviceId) {

        return licenseService.activateLicense(
                licenseKey,
                deviceId
        );
    }

    @GetMapping("/verify")
    public TicketResponse verifyLicense(@RequestParam String licenseKey,
                                        @RequestParam String deviceId) {

        return licenseService.verifyLicense(
                licenseKey,
                deviceId
        );
    }

    @PostMapping("/renew")
    public License renewLicense(@RequestParam String licenseKey,
                                @RequestParam int extraDays) {

        return licenseService.renewLicense(
                licenseKey,
                extraDays
        );
    }
}