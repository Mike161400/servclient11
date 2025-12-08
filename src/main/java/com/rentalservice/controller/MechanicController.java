package com.rentalservice.controller;

import com.rentalservice.model.Mechanic;
import com.rentalservice.service.MechanicService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mechanics")
public class MechanicController {
    private final MechanicService mechanicService;

    public MechanicController(MechanicService mechanicService) {
        this.mechanicService = mechanicService;
    }

    // 📋 GET /mechanics - получить всех механиков
    @GetMapping
    public List<Mechanic> getAll() {
        return mechanicService.getAll();
    }

    // 🔍 GET /mechanics/{id} - получить механика по ID
    @GetMapping("/{id}")
    public Mechanic getById(@PathVariable Long id) {
        return mechanicService.getById(id);
    }

    // ➕ POST /mechanics - создать механика
    @PostMapping
    public Mechanic create(@RequestBody Mechanic mechanic) {
        return mechanicService.create(mechanic);
    }

    // ✏️ PUT /mechanics/{id} - обновить данные механика
    @PutMapping("/{id}")
    public Mechanic update(@PathVariable Long id, @RequestBody Mechanic mechanic) {
        return mechanicService.update(id, mechanic);
    }

    // 🗑️ DELETE /mechanics/{id} - удалить механика
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return mechanicService.delete(id);
    }
}