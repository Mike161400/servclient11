package com.rentalservice.service;

import com.rentalservice.model.Vehicle;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class VehicleService {
    private final Map<Long, Vehicle> vehicles = new HashMap<>();
    private long nextId = 1;

    // ➕ Создать автомобиль
    public Vehicle create(Vehicle vehicle) {
        vehicle.setId(nextId++);
        vehicles.put(vehicle.getId(), vehicle);
        return vehicle;
    }

    // 📋 Получить все автомобили
    public List<Vehicle> getAll() {
        return new ArrayList<>(vehicles.values());
    }

    // 🔍 Найти автомобиль по ID
    public Vehicle getById(Long id) {
        return vehicles.get(id);
    }

    // ✏️ Обновить данные автомобиля
    public Vehicle update(Long id, Vehicle updated) {
        Vehicle existing = vehicles.get(id);
        if (existing != null) {
            updated.setId(id); // сохраняем оригинальный ID
            vehicles.put(id, updated);
            return updated;
        }
        return null; // автомобиль не найден
    }

    // 🗑️ Удалить автомобиль
    public boolean delete(Long id) {
        return vehicles.remove(id) != null;
    }

    // 🚗 Найти автомобили клиента
    public List<Vehicle> getVehiclesByCustomerId(Long customerId) {
        return vehicles.values().stream()
                .filter(vehicle -> vehicle.getCustomerId().equals(customerId))
                .toList();
    }
}