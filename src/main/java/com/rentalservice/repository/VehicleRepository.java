package com.rentalservice.repository;

import com.rentalservice.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByCustomerId(Long customerId);
    boolean existsByLicensePlate(String licensePlate);
    boolean existsByVin(String vin);
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    Optional<Vehicle> findByVin(String vin);

    @Query("SELECT v FROM Vehicle v WHERE v.brand = :brand")
    List<Vehicle> findByBrand(String brand);

    @Query("SELECT v FROM Vehicle v WHERE v.year >= :minYear AND v.year <= :maxYear")
    List<Vehicle> findByYearBetween(int minYear, int maxYear);
}