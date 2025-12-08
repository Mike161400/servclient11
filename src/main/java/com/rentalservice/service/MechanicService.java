package com.rentalservice.service;

import com.rentalservice.model.Mechanic;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MechanicService {
    private List<Mechanic> mechanics = new ArrayList<>();
    private Long nextId = 1L;

    // 📋 Получить всех механиков
    public List<Mechanic> getAll() {
        return new ArrayList<>(mechanics);
    }

    // ➕ Создать механика
    public Mechanic create(Mechanic mechanic) {
        mechanic.setId(nextId++);
        mechanics.add(mechanic);
        return mechanic;
    }

    // ✏️ Обновить данные механика
    public Mechanic update(Long id, Mechanic updatedMechanic) {
        Optional<Mechanic> existing = mechanics.stream()
                .filter(mechanic -> mechanic.getId().equals(id))
                .findFirst();

        if (existing.isPresent()) {
            Mechanic mechanic = existing.get();
            mechanic.setName(updatedMechanic.getName());
            mechanic.setPhoneNumber(updatedMechanic.getPhoneNumber());
            mechanic.setEmail(updatedMechanic.getEmail());
            mechanic.setSpecialization(updatedMechanic.getSpecialization());
            mechanic.setExperienceYears(updatedMechanic.getExperienceYears());
            return mechanic;
        } else {
            return null; // механик не найден
        }
    }

    // 🗑️ Удалить механика
    public boolean delete(Long id) {
        return mechanics.removeIf(mechanic -> mechanic.getId().equals(id));
    }

    // 🔍 Найти механика по ID
    public Mechanic getById(Long id) {
        return mechanics.stream()
                .filter(mechanic -> mechanic.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}