package com.rentalservice.service;

import com.rentalservice.model.Part;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class PartService {
    private final List<Part> parts = new ArrayList<>();
    private Long nextId = 1L;

    // 📋 Получить все детали
    public List<Part> getAllParts() {
        return new ArrayList<>(parts);
    }

    // ➕ Создать деталь
    public Part createPart(Part part) {
        part.setId(nextId++);
        parts.add(part);
        return part;
    }

    // ✏️ Обновить деталь
    public Part updatePart(Long id, Part updatedPart) {
        for (Part part : parts) {
            if (part.getId().equals(id)) {
                part.setName(updatedPart.getName());
                part.setPartNumber(updatedPart.getPartNumber());
                part.setDescription(updatedPart.getDescription());
                part.setPrice(updatedPart.getPrice());
                part.setQuantityInStock(updatedPart.getQuantityInStock());
                return part;
            }
        }
        return null; // деталь не найдена
    }

    // 🗑️ Удалить деталь
    public boolean deletePart(Long id) {
        return parts.removeIf(part -> part.getId().equals(id));
    }

    // 🔍 Найти деталь по ID
    public Part getPartById(Long id) {
        return parts.stream()
                .filter(part -> part.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // 💰 Получить цену детали (для расчета стоимости заказа)
    public double getPartPrice(Long partId) {
        Part part = getPartById(partId);
        return part != null ? part.getPrice() : 0.0;
    }
}