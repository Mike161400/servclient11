package com.rentalservice.service;

import com.rentalservice.model.Mechanic;
import com.rentalservice.repository.MechanicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class MechanicService {

    private final MechanicRepository mechanicRepository;

    public MechanicService(MechanicRepository mechanicRepository) {
        this.mechanicRepository = mechanicRepository;
    }

    public List<Mechanic> getAll() {
        return mechanicRepository.findAll();
    }

    public Mechanic create(Mechanic mechanic) {
        // Проверяем уникальность email и телефона
        if (mechanicRepository.existsByEmail(mechanic.getEmail())) {
            throw new IllegalArgumentException("Механик с таким email уже существует");
        }
        if (mechanicRepository.existsByPhoneNumber(mechanic.getPhoneNumber())) {
            throw new IllegalArgumentException("Механик с таким номером телефона уже существует");
        }

        return mechanicRepository.save(mechanic);
    }

    public Mechanic update(Long id, Mechanic updatedMechanic) {
        return mechanicRepository.findById(id)
                .map(mechanic -> {
                    // Проверяем уникальность email если изменился
                    if (!mechanic.getEmail().equals(updatedMechanic.getEmail())
                            && mechanicRepository.existsByEmail(updatedMechanic.getEmail())) {
                        throw new IllegalArgumentException("Механик с таким email уже существует");
                    }

                    // Проверяем уникальность телефона если изменился
                    if (!mechanic.getPhoneNumber().equals(updatedMechanic.getPhoneNumber())
                            && mechanicRepository.existsByPhoneNumber(updatedMechanic.getPhoneNumber())) {
                        throw new IllegalArgumentException("Механик с таким номером телефона уже существует");
                    }

                    mechanic.setName(updatedMechanic.getName());
                    mechanic.setPhoneNumber(updatedMechanic.getPhoneNumber());
                    mechanic.setEmail(updatedMechanic.getEmail());
                    mechanic.setSpecialization(updatedMechanic.getSpecialization());
                    mechanic.setExperienceYears(updatedMechanic.getExperienceYears());
                    mechanic.setHourlyRate(updatedMechanic.getHourlyRate());
                    mechanic.setActive(updatedMechanic.isActive());

                    return mechanicRepository.save(mechanic);
                })
                .orElseThrow(() -> new IllegalArgumentException("Механик не найден"));
    }

    public boolean delete(Long id) {
        if (mechanicRepository.existsById(id)) {
            mechanicRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Mechanic getById(Long id) {
        return mechanicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Механик не найден"));
    }
}