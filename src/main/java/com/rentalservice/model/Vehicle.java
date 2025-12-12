package com.rentalservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Госномер обязателен")
    @Pattern(regexp = "^[АВЕКМНОРСТУХавекмнорстух0-9]{6,9}$",
            message = "Некорректный госномер")
    @Column(name = "license_plate", nullable = false, unique = true)
    private String licensePlate;

    @NotBlank(message = "Марка обязательна")
    @Column(nullable = false)
    private String brand;

    @NotBlank(message = "Модель обязательна")
    @Column(nullable = false)
    private String model;

    @Min(value = 1900, message = "Год должен быть больше 1900")
    @Column(nullable = false)
    private int year;

    @NotBlank(message = "VIN обязателен")
    @Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$", message = "Некорректный VIN (17 символов)")
    @Column(nullable = false, unique = true, length = 17)
    private String vin;

    @NotNull(message = "Владелец обязателен")
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Конструкторы
    public Vehicle() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Vehicle(String licensePlate, String brand, String model,
                   int year, String vin, Long customerId) {
        this();
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.vin = vin;
        this.customerId = customerId;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}