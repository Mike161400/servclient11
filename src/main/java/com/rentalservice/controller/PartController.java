package com.rentalservice.controller;

import com.rentalservice.model.Part;
import com.rentalservice.service.PartService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/parts")
public class PartController {

    private final PartService partService;

    public PartController(PartService partService) {
        this.partService = partService;
    }

    // 📋 GET /parts - получить все детали
    @GetMapping
    public List<Part> getAll() {
        return partService.getAllParts();
    }

    // 🔍 GET /parts/{id} - получить деталь по ID
    @GetMapping("/{id}")
    public Part getById(@PathVariable Long id) {
        return partService.getPartById(id);
    }

    // ➕ POST /parts - создать деталь
    @PostMapping
    public Part create(@RequestBody Part part) {
        return partService.createPart(part);
    }

    // ✏️ PUT /parts/{id} - обновить деталь
    @PutMapping("/{id}")
    public Part update(@PathVariable Long id, @RequestBody Part part) {
        return partService.updatePart(id, part);
    }

    // 🗑️ DELETE /parts/{id} - удалить деталь
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return partService.deletePart(id);
    }
}