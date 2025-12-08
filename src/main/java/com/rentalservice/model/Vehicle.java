package com.rentalservice.model;

public class Vehicle {
    private Long id;
    private String licensePlate;    // госномер
    private String brand;           // марка
    private String model;           // модель
    private int year;               // год выпуска
    private String vin;             // VIN код
    private Long customerId;        // владелец

    public Vehicle() {}

    public Vehicle(Long id, String licensePlate, String brand, String model,
                   int year, String vin, Long customerId) {
        this.id = id;
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
}