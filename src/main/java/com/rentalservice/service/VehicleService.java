package com.rentalservice.service;

import com.rentalservice.model.Vehicle;
import com.rentalservice.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    // 💉 Внедряем зависимость через конструктор
    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    // ➕ Создать автомобиль
    public Vehicle create(Vehicle vehicle) {
        // Проверяем уникальность госномера и VIN
        if (vehicleRepository.existsByLicensePlate(vehicle.getLicensePlate())) {
            throw new IllegalArgumentException("Автомобиль с таким госномером уже существует");
        }
        if (vehicleRepository.existsByVin(vehicle.getVin())) {
            throw new IllegalArgumentException("Автомобиль с таким VIN уже существует");
        }

        return vehicleRepository.save(vehicle);
    }

    // 📋 Получить все автомобили
    public List<Vehicle> getAll() {
        return vehicleRepository.findAll();
    }

    // 🔍 Найти автомобиль по ID
    public Vehicle getById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден"));
    }

    // ✏️ Обновить данные автомобиля
    public Vehicle update(Long id, Vehicle updated) {
        return vehicleRepository.findById(id)
                .map(vehicle -> {
                    // Проверяем уникальность госномера если изменился
                    if (!vehicle.getLicensePlate().equals(updated.getLicensePlate())
                            && vehicleRepository.existsByLicensePlate(updated.getLicensePlate())) {
                        throw new IllegalArgumentException("Автомобиль с таким госномером уже существует");
                    }

                    // Проверяем уникальность VIN если изменился
                    if (!vehicle.getVin().equals(updated.getVin())
                            && vehicleRepository.existsByVin(updated.getVin())) {
                        throw new IllegalArgumentException("Автомобиль с таким VIN уже существует");
                    }

                    vehicle.setLicensePlate(updated.getLicensePlate());
                    vehicle.setBrand(updated.getBrand());
                    vehicle.setModel(updated.getModel());
                    vehicle.setYear(updated.getYear());
                    vehicle.setVin(updated.getVin());
                    vehicle.setCustomerId(updated.getCustomerId());

                    return vehicleRepository.save(vehicle);
                })
                .orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден"));
    }

    // 🗑️ Удалить автомобиль
    public boolean delete(Long id) {
        if (vehicleRepository.existsById(id)) {
            vehicleRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // 🚗 Найти автомобили клиента
    public List<Vehicle> getVehiclesByCustomerId(Long customerId) {
        return vehicleRepository.findByCustomerId(customerId);
    }
}