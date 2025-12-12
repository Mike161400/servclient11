package com.rentalservice.controller;

import com.rentalservice.model.ServiceOrder;
import com.rentalservice.service.BusinessOperationsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/business")
public class BusinessOperationsController {

    private final BusinessOperationsService businessService;

    public BusinessOperationsController(BusinessOperationsService businessService) {
        this.businessService = businessService;
    }

    // 🚗 ОПЕРАЦИЯ 1: Создание полного заказа
    @PostMapping("/full-order")
    public ServiceOrder createFullOrder(
            @RequestParam Long vehicleId,
            @RequestParam Long mechanicId,
            @RequestBody OrderRequest request) {

        return businessService.createFullServiceOrder(
                vehicleId, mechanicId, request.getServices(), request.getPartIds());
    }

    // 💰 ОПЕРАЦИЯ 2: Генерация счета
    @GetMapping("/invoice/{orderId}")
    public Map<String, Object> generateInvoice(@PathVariable Long orderId) {
        return businessService.generateInvoice(orderId);
    }

    // 📊 ОПЕРАЦИЯ 3: Отчет по механикам
    @GetMapping("/performance-report")
    public Map<String, Object> getPerformanceReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return businessService.getMechanicPerformanceReport(startDate, endDate);
    }

    // 🚨 ОПЕРАЦИЯ 4: Проверка запасов
    @GetMapping("/low-stock")
    public List<Map<String, Object>> checkLowStock(@RequestParam(defaultValue = "5") int threshold) {
        return businessService.checkAndOrderLowStockParts(threshold);
    }

    // 📝 ОПЕРАЦИЯ 5: Быстрое ТО
    @PostMapping("/quick-maintenance")
    public ServiceOrder quickMaintenance(
            @RequestParam Long vehicleId,
            @RequestParam(defaultValue = "STANDARD") String type) {

        return businessService.quickMaintenance(vehicleId, type);
    }

    // DTO для запроса
    public static class OrderRequest {
        private List<String> services;
        private List<Long> partIds;

        public List<String> getServices() { return services; }
        public void setServices(List<String> services) { this.services = services; }

        public List<Long> getPartIds() { return partIds; }
        public void setPartIds(List<Long> partIds) { this.partIds = partIds; }
    }
}