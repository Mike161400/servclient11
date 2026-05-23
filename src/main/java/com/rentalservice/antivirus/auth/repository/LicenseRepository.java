package com.rentalservice.antivirus.auth.repository;

import com.rentalservice.antivirus.auth.entity.License;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LicenseRepository extends JpaRepository<License, UUID> {

    Optional<License> findByLicenseKey(String licenseKey);
}