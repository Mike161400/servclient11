package com.rentalservice.repository;

import com.rentalservice.model.Mechanic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MechanicRepository extends JpaRepository<Mechanic, Long> {
    Optional<Mechanic> findByEmail(String email);
    Optional<Mechanic> findByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

    List<Mechanic> findByActiveTrue();
    List<Mechanic> findBySpecialization(String specialization);

    @Query("SELECT m FROM Mechanic m WHERE m.experienceYears >= :minExperience")
    List<Mechanic> findByExperienceYearsGreaterThanEqual(int minExperience);

    @Query("SELECT m FROM Mechanic m WHERE m.hourlyRate BETWEEN :minRate AND :maxRate")
    List<Mechanic> findByHourlyRateBetween(double minRate, double maxRate);
}