package com.rentalservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "parts")
public class Part {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название детали обязательно")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Артикул обязателен")
    @Column(name = "part_number", nullable = false, unique = true)
    private String partNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @DecimalMin(value = "0.0", message = "Цена не может быть отрицательной")
    @Column(nullable = false)
    private double price;

    @Min(value = 0, message = "Количество не может быть отрицательным")
    @Column(name = "quantity_in_stock", nullable = false)
    private int quantityInStock = 0;

    @Min(value = 0, message = "Минимальное количество не может быть отрицательным")
    @Column(name = "min_quantity")
    private int minQuantity = 5;

    @Column(length = 100)
    private String supplier;

    @Column(length = 50)
    private String category;

    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    // Конструкторы
    public Part() {
        this.createdAt = java.time.LocalDateTime.now();
        this.updatedAt = java.time.LocalDateTime.now();
    }

    public Part(String name, String partNumber, String description,
                double price, int quantityInStock, String category) {
        this();
        this.name = name;
        this.partNumber = partNumber;
        this.description = description;
        this.price = price;
        this.quantityInStock = quantityInStock;
        this.category = category;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(int quantityInStock) { this.quantityInStock = quantityInStock; }

    public int getMinQuantity() { return minQuantity; }
    public void setMinQuantity(int minQuantity) { this.minQuantity = minQuantity; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = java.time.LocalDateTime.now();
    }
}