package com.rentalservice.repository;

import com.rentalservice.model.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {
    List<ServiceOrder> findByVehicleId(Long vehicleId);
    List<ServiceOrder> findByMechanicId(Long mechanicId);
    List<ServiceOrder> findByStatus(String status);

    @Query("SELECT COUNT(so) FROM ServiceOrder so WHERE so.status = :status")
    long countByStatus(String status);

    @Query("SELECT so FROM ServiceOrder so WHERE so.creationDate BETWEEN :startDate AND :endDate")
    List<ServiceOrder> findByCreationDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT so FROM ServiceOrder so WHERE so.completionDate BETWEEN :startDate AND :endDate")
    List<ServiceOrder> findByCompletionDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT so FROM ServiceOrder so WHERE so.creationDate = :date")
    List<ServiceOrder> findByCreationDate(LocalDate date);

    @Query("SELECT so FROM ServiceOrder so WHERE so.mechanicId = :mechanicId AND so.status = :status")
    List<ServiceOrder> findByMechanicIdAndStatus(Long mechanicId, String status);

    @Query("SELECT so FROM ServiceOrder so WHERE so.vehicleId IN (SELECT v.id FROM Vehicle v WHERE v.customerId = :customerId)")
    List<ServiceOrder> findByCustomerId(Long customerId);

    @Query("SELECT so FROM ServiceOrder so WHERE so.creationDate >= :date")
    List<ServiceOrder> findOrdersAfterDate(LocalDate date);
}