package com.rentalservice.service;

import com.rentalservice.model.ServiceOrder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceOrderService {
    private final List<ServiceOrder> serviceOrders = new ArrayList<>();
    private Long nextId = 1L;
    private final PartService partService;

    // 📝 Конструктор с зависимостью от PartService (Dependency Injection)
    public ServiceOrderService(PartService partService) {
        this.partService = partService;
    }

    // 📋 Получить все заказы
    public List<ServiceOrder> getAllServiceOrders() {
        return new ArrayList<>(serviceOrders);
    }

    // ➕ Создать новый заказ-наряд
    public ServiceOrder createServiceOrder(ServiceOrder serviceOrder) {
        serviceOrder.setId(nextId++);
        serviceOrder.setStatus("CREATED"); // Устанавливаем статус при создании
        serviceOrders.add(serviceOrder);
        return serviceOrder;
    }

    // ✏️ Обновить заказ
    public ServiceOrder updateServiceOrder(Long id, ServiceOrder updatedServiceOrder) {
        for (ServiceOrder order : serviceOrders) {
            if (order.getId().equals(id)) {
                // Обновляем все поля кроме ID
                order.setVehicleId(updatedServiceOrder.getVehicleId());
                order.setMechanicId(updatedServiceOrder.getMechanicId());
                order.setCreationDate(updatedServiceOrder.getCreationDate());
                order.setCompletionDate(updatedServiceOrder.getCompletionDate());
                order.setStatus(updatedServiceOrder.getStatus());
                order.setLaborCost(updatedServiceOrder.getLaborCost());

                // Создаем новые списки чтобы избежать shared mutable state
                order.setPartIds(new ArrayList<>(updatedServiceOrder.getPartIds()));
                order.setRequiredServices(new ArrayList<>(updatedServiceOrder.getRequiredServices()));
                order.setCompletedServices(new ArrayList<>(updatedServiceOrder.getCompletedServices()));

                return order;
            }
        }
        return null; // заказ не найден
    }

    // 🗑️ Удалить заказ
    public boolean deleteServiceOrder(Long id) {
        return serviceOrders.removeIf(order -> order.getId().equals(id));
    }

    // 🔍 Найти заказ по ID
    public ServiceOrder getServiceOrderById(Long id) {
        return serviceOrders.stream()
                .filter(order -> order.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // 💰 ОСНОВНАЯ ЛОГИКА: Расчет общей стоимости заказа
    public double calculateTotalCost(Long orderId) {
        ServiceOrder order = getServiceOrderById(orderId);
        if (order == null) return 0.0;

        // Сумма стоимости всех деталей в заказе
        double partsCost = order.getPartIds().stream()
                .mapToDouble(partService::getPartPrice) // для каждого ID детали получаем цену
                .sum();

        // Общая стоимость = стоимость деталей + стоимость работ
        return partsCost + order.getLaborCost();
    }

    // ✅ Добавить выполненную работу в заказ
    public ServiceOrder addCompletedService(Long orderId, String service) {
        ServiceOrder order = getServiceOrderById(orderId);
        if (order != null) {
            order.addCompletedService(service);

            // Автоматически меняем статус на "IN_PROGRESS" если начали работы
            if ("CREATED".equals(order.getStatus())) {
                order.setStatus("IN_PROGRESS");
            }
            return order;
        }
        return null; // заказ не найден
    }

    // 🚗 Закрыть заказ (только если все обязательные работы выполнены)
    public ServiceOrder completeOrder(Long orderId) {
        ServiceOrder order = getServiceOrderById(orderId);
        if (order != null && order.canBeCompleted()) {
            order.setStatus("COMPLETED");
            order.setCompletionDate(java.time.LocalDate.now().toString());
            return order;
        }
        return null; // Нельзя закрыть - не все работы выполнены
    }

    // 📊 Получить заказы по статусу
    public List<ServiceOrder> getOrdersByStatus(String status) {
        return serviceOrders.stream()
                .filter(order -> order.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    // 👨‍🔧 Получить заказы по механику
    public List<ServiceOrder> getOrdersByMechanic(Long mechanicId) {
        return serviceOrders.stream()
                .filter(order -> order.getMechanicId().equals(mechanicId))
                .collect(Collectors.toList());
    }

    // 🔍 Проверить можно ли закрыть заказ
    public boolean canCompleteOrder(Long orderId) {
        ServiceOrder order = getServiceOrderById(orderId);
        return order != null && order.canBeCompleted();
    }

    // 📈 Получить статистику по заказам
    public String getOrderStatistics() {
        long created = getOrdersByStatus("CREATED").size();
        long inProgress = getOrdersByStatus("IN_PROGRESS").size();
        long completed = getOrdersByStatus("COMPLETED").size();

        return String.format("Заказы: CREATED=%d, IN_PROGRESS=%d, COMPLETED=%d",
                created, inProgress, completed);
    }
}