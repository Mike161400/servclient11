package com.rentalservice.repository;

import com.rentalservice.model.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PartRepository extends JpaRepository<Part, Long> {
    Optional<Part> findByPartNumber(String partNumber);
    boolean existsByPartNumber(String partNumber);

    List<Part> findByCategory(String category);
    List<Part> findBySupplier(String supplier);

    @Query("SELECT p FROM Part p WHERE p.quantityInStock < p.minQuantity")
    List<Part> findLowStockParts();

    List<Part> findByQuantityInStockLessThan(int quantity);

    @Query("SELECT p FROM Part p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Part> findByPriceBetween(double minPrice, double maxPrice);

    @Query("SELECT p FROM Part p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Part> findByNameContainingIgnoreCase(String name);
}