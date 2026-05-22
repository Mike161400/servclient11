package com.rentalservice.antivirus.auth.repository;

import com.rentalservice.antivirus.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenValue(String tokenValue);

    Optional<RefreshToken> findByTokenValueAndRevokedFalse(String tokenValue);

    List<RefreshToken> findAllByUser_Id(UUID userId);

    void deleteByUser_Id(UUID userId);
}
