package com.rentalservice.model;

public class Customer {
    private Long id;
    private String name;
    private String phoneNumber;
    private String email;
    private String address;

    // Конструктор по умолчанию (нужен для Spring)
    public Customer() {}

    // Конструктор со всеми полями
    public Customer(Long id, String name, String phoneNumber, String email, String address) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
    }

    // ⚡ ГЕТТЕРЫ и СЕТТЕРЫ ⚡
    // (Spring использует их для работы с JSON)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}