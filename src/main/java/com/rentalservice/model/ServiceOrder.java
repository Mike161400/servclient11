package com.rentalservice.model;

import java.util.List;
import java.util.ArrayList;

public class ServiceOrder {
    private Long id;
    private Long vehicleId;          // какой автомобиль
    private Long mechanicId;         // какой механик
    private String creationDate;     // дата создания
    private String completionDate;   // дата завершения
    private String status;           // статус: "CREATED", "IN_PROGRESS", "COMPLETED"
    private double laborCost;        // стоимость работ
    private List<Long> partIds;      // список ID использованных деталей
    private List<String> requiredServices;   // обязательные работы
    private List<String> completedServices;  // выполненные работы

    // Конструктор по умолчанию - инициализируем списки
    public ServiceOrder() {
        this.partIds = new ArrayList<>();
        this.requiredServices = new ArrayList<>();
        this.completedServices = new ArrayList<>();
        this.status = "CREATED"; // при создании статус "Создан"
    }

    // Конструктор с основными полями
    public ServiceOrder(Long id, Long vehicleId, Long mechanicId, String creationDate,
                        String completionDate, String status, double laborCost) {
        this(); // вызываем конструктор по умолчанию для инициализации списков
        this.id = id;
        this.vehicleId = vehicleId;
        this.mechanicId = mechanicId;
        this.creationDate = creationDate;
        this.completionDate = completionDate;
        this.status = status;
        this.laborCost = laborCost;
    }

    // ⚡ ГЕТТЕРЫ и СЕТТЕРЫ ⚡
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public Long getMechanicId() { return mechanicId; }
    public void setMechanicId(Long mechanicId) { this.mechanicId = mechanicId; }

    public String getCreationDate() { return creationDate; }
    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    public String getCompletionDate() { return completionDate; }
    public void setCompletionDate(String completionDate) { this.completionDate = completionDate; }

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

    // 🔧 ОСНОВНАЯ ЛОГИКА: Проверка можно ли закрыть заказ
    public boolean canBeCompleted() {
        // Проверяем что ВСЕ обязательные работы выполнены
        return requiredServices.stream()
                .allMatch(service -> completedServices.contains(service));
    }

    // ✅ Метод для добавления выполненной работы
    public void addCompletedService(String service) {
        if (!completedServices.contains(service)) {
            completedServices.add(service);
        }
    }

    // ➕ Метод для добавления детали в заказ
    public void addPart(Long partId) {
        if (!partIds.contains(partId)) {
            partIds.add(partId);
        }
    }

    // ➕ Метод для добавления обязательной работы
    public void addRequiredService(String service) {
        if (!requiredServices.contains(service)) {
            requiredServices.add(service);
        }
    }
}