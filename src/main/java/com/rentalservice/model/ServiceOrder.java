package com.rentalservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_orders")
public class ServiceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Автомобиль обязателен")
    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @NotNull(message = "Механик обязателен")
    @Column(name = "mechanic_id", nullable = false)
    private Long mechanicId;

    @Column(name = "creation_date", nullable = false)
    private LocalDate creationDate;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @NotBlank(message = "Статус обязателен")
    @Column(nullable = false, length = 20)
    private String status = "CREATED";

    @DecimalMin(value = "0.0", message = "Стоимость работ не может быть отрицательной")
    @Column(name = "labor_cost")
    private double laborCost = 0.0;

    @ElementCollection
    @CollectionTable(name = "order_parts", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "part_id")
    private List<Long> partIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "required_services", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "service", columnDefinition = "TEXT")
    private List<String> requiredServices = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "completed_services", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "service", columnDefinition = "TEXT")
    private List<String> completedServices = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Конструкторы
    public ServiceOrder() {
        this.creationDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public ServiceOrder(Long vehicleId, Long mechanicId, double laborCost) {
        this();
        this.vehicleId = vehicleId;
        this.mechanicId = mechanicId;
        this.laborCost = laborCost;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public Long getMechanicId() { return mechanicId; }
    public void setMechanicId(Long mechanicId) { this.mechanicId = mechanicId; }

    public LocalDate getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }

    public LocalDate getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDate completionDate) { this.completionDate = completionDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getLaborCost() { return laborCost; }
    public void setLaborCost(double laborCost) { this.laborCost = laborCost; }

    public List<Long> getPartIds() { return partIds; }
    public void setPartIds(List<Long> partIds) { this.partIds = partIds; }

    public List<String> getRequiredServices() { return requiredServices; }
    public void setRequiredServices(List<String> requiredServices) { this.requiredServices = requiredServices; }

    public List<String> getCompletedServices() { return completedServices; }
    public void setCompletedServices(List<String> completedServices) { this.completedServices = completedServices; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Бизнес-логика
    public boolean canBeCompleted() {
        return requiredServices.stream()
                .allMatch(service -> completedServices.contains(service));
    }

    public void addCompletedService(String service) {
        if (!completedServices.contains(service)) {
            completedServices.add(service);
        }
    }

    public void addPart(Long partId) {
        if (!partIds.contains(partId)) {
            partIds.add(partId);
        }
    }

    public void addRequiredService(String service) {
        if (!requiredServices.contains(service)) {
            requiredServices.add(service);
        }
    }
}