package com.rentalservice.model;

public class Part {
    private Long id;
    private String name;            // название детали
    private String partNumber;      // артикул
    private String description;     // описание
    private double price;           // цена
    private int quantityInStock;    // количество на складе

    public Part() {}

    public Part(Long id, String name, String partNumber, String description,
                double price, int quantityInStock) {
        this.id = id;
        this.name = name;
        this.partNumber = partNumber;
        this.description = description;
        this.price = price;
        this.quantityInStock = quantityInStock;
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
}