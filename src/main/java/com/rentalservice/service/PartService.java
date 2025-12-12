package com.rentalservice.service;

import com.rentalservice.model.Part;
import com.rentalservice.repository.PartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class PartService {

    private final PartRepository partRepository;

    public PartService(PartRepository partRepository) {
        this.partRepository = partRepository;
    }

    public List<Part> getAllParts() {
        return partRepository.findAll();
    }

    public Part createPart(Part part) {
        // Проверяем уникальность артикула
        if (partRepository.existsByPartNumber(part.getPartNumber())) {
            throw new IllegalArgumentException("Деталь с таким артикулом уже существует");
        }

        return partRepository.save(part);
    }

    public Part updatePart(Long id, Part updatedPart) {
        return partRepository.findById(id)
                .map(part -> {
                    // Проверяем уникальность артикула если изменился
                    if (!part.getPartNumber().equals(updatedPart.getPartNumber())
                            && partRepository.existsByPartNumber(updatedPart.getPartNumber())) {
                        throw new IllegalArgumentException("Деталь с таким артикулом уже существует");
                    }

                    part.setName(updatedPart.getName());
                    part.setPartNumber(updatedPart.getPartNumber());
                    part.setDescription(updatedPart.getDescription());
                    part.setPrice(updatedPart.getPrice());
                    part.setQuantityInStock(updatedPart.getQuantityInStock());
                    part.setMinQuantity(updatedPart.getMinQuantity());
                    part.setSupplier(updatedPart.getSupplier());
                    part.setCategory(updatedPart.getCategory());

                    return partRepository.save(part);
                })
                .orElseThrow(() -> new IllegalArgumentException("Деталь не найдена"));
    }

    public boolean deletePart(Long id) {
        if (partRepository.existsById(id)) {
            partRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Part getPartById(Long id) {
        return partRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Деталь не найдена"));
    }

    public double getPartPrice(Long partId) {
        Part part = getPartById(partId);
        return part.getPrice();
    }
}