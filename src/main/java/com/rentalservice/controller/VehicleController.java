package com.rentalservice.controller;

import com.rentalservice.model.Vehicle;
import com.rentalservice.service.VehicleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    // ➕ POST /vehicles - создать автомобиль
    @PostMapping
    public Vehicle createVehicle(@RequestBody Vehicle vehicle) {
        return vehicleService.create(vehicle);
    }

    // 📋 GET /vehicles - получить все автомобили
    @GetMapping
    public List<Vehicle> getAllVehicles() {
        return vehicleService.getAll();
    }

    // 🔍 GET /vehicles/{id} - получить автомобиль по ID
    @GetMapping("/{id}")
    public Vehicle getVehicleById(@PathVariable Long id) {
        return vehicleService.getById(id);
    }

    // 🚗 GET /vehicles/customer/{customerId} - получить авто клиента
    @GetMapping("/customer/{customerId}")
    public List<Vehicle> getVehiclesByCustomer(@PathVariable Long customerId) {
        return vehicleService.getVehiclesByCustomerId(customerId);
    }

    // ✏️ PUT /vehicles/{id} - обновить данные автомобиля
    @PutMapping("/{id}")
    public Vehicle updateVehicle(@PathVariable Long id, @RequestBody Vehicle vehicle) {
        return vehicleService.update(id, vehicle);
    }

    // 🗑️ DELETE /vehicles/{id} - удалить автомобиль
    @DeleteMapping("/{id}")
    public String deleteVehicle(@PathVariable Long id) {
        return vehicleService.delete(id) ? "Vehicle deleted" : "Vehicle not found";
    }
}