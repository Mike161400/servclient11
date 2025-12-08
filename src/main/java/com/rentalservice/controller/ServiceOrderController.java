package com.rentalservice.controller;

import com.rentalservice.model.ServiceOrder;
import com.rentalservice.service.ServiceOrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/service-orders")
public class ServiceOrderController {

    private final ServiceOrderService serviceOrderService;

    // 💉 Внедряем зависимость ServiceOrderService
    public ServiceOrderController(ServiceOrderService serviceOrderService) {
        this.serviceOrderService = serviceOrderService;
    }

    // 📋 GET /service-orders - получить все заказы
    @GetMapping
    public List<ServiceOrder> getAll() {
        return serviceOrderService.getAllServiceOrders();
    }

    // 🔍 GET /service-orders/{id} - получить заказ по ID
    @GetMapping("/{id}")
    public ServiceOrder getById(@PathVariable Long id) {
        return serviceOrderService.getServiceOrderById(id);
    }

    // 💰 GET /service-orders/{id}/total-cost - рассчитать стоимость заказа
    @GetMapping("/{id}/total-cost")
    public double getTotalCost(@PathVariable Long id) {
        return serviceOrderService.calculateTotalCost(id);
    }

    // 📊 GET /service-orders/status/{status} - получить заказы по статусу
    @GetMapping("/status/{status}")
    public List<ServiceOrder> getByStatus(@PathVariable String status) {
        return serviceOrderService.getOrdersByStatus(status);
    }

    // 👨‍🔧 GET /service-orders/mechanic/{mechanicId} - получить заказы механика
    @GetMapping("/mechanic/{mechanicId}")
    public List<ServiceOrder> getByMechanic(@PathVariable Long mechanicId) {
        return serviceOrderService.getOrdersByMechanic(mechanicId);
    }

    // ❓ GET /service-orders/{id}/can-complete - можно ли закрыть заказ
    @GetMapping("/{id}/can-complete")
    public boolean canCompleteOrder(@PathVariable Long id) {
        return serviceOrderService.canCompleteOrder(id);
    }

    // 📈 GET /service-orders/statistics - статистика по заказам
    @GetMapping("/statistics")
    public String getStatistics() {
        return serviceOrderService.getOrderStatistics();
    }

    // ➕ POST /service-orders - создать новый заказ
    @PostMapping
    public ServiceOrder create(@RequestBody ServiceOrder serviceOrder) {
        return serviceOrderService.createServiceOrder(serviceOrder);
    }

    // ✏️ PUT /service-orders/{id} - обновить заказ
    @PutMapping("/{id}")
    public ServiceOrder update(@PathVariable Long id, @RequestBody ServiceOrder serviceOrder) {
        return serviceOrderService.updateServiceOrder(id, serviceOrder);
    }

    // ✅ PUT /service-orders/{id}/complete-service - добавить выполненную работу
    @PutMapping("/{id}/complete-service")
    public ServiceOrder addCompletedService(@PathVariable Long id, @RequestBody String service) {
        return serviceOrderService.addCompletedService(id, service);
    }

    // 🚗 PUT /service-orders/{id}/complete-order - закрыть заказ
    @PutMapping("/{id}/complete-order")
    public ServiceOrder completeOrder(@PathVariable Long id) {
        return serviceOrderService.completeOrder(id);
    }

    // 🗑️ DELETE /service-orders/{id} - удалить заказ
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return serviceOrderService.deleteServiceOrder(id);
    }
}