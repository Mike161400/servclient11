package com.rentalservice.model;

public class Mechanic {
    private Long id;
    private String name;
    private String phoneNumber;
    private String email;
    private String specialization;  // специализация
    private int experienceYears;    // опыт работы

    public Mechanic() {}

    public Mechanic(Long id, String name, String phoneNumber, String email,
                    String specialization, int experienceYears) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.specialization = specialization;
        this.experienceYears = experienceYears;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }
}