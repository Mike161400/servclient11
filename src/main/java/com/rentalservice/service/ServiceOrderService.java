package com.rentalservice.service;

import com.rentalservice.model.ServiceOrder;
import com.rentalservice.repository.ServiceOrderRepository;
import com.rentalservice.repository.PartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServiceOrderService {

    private final ServiceOrderRepository serviceOrderRepository;
    private final PartRepository partRepository;

    public ServiceOrderService(ServiceOrderRepository serviceOrderRepository,
                               PartRepository partRepository) {
        this.serviceOrderRepository = serviceOrderRepository;
        this.partRepository = partRepository;
    }

    // 📋 Получить все заказы
    public List<ServiceOrder> getAllServiceOrders() {
        return serviceOrderRepository.findAll();
    }

    // ➕ Создать новый заказ-наряд
    public ServiceOrder createServiceOrder(ServiceOrder serviceOrder) {
        if (serviceOrder.getCreationDate() == null) {
            serviceOrder.setCreationDate(LocalDate.now());
        }
        serviceOrder.setStatus("CREATED");
        return serviceOrderRepository.save(serviceOrder);
    }

    // ✏️ Обновить заказ
    public ServiceOrder updateServiceOrder(Long id, ServiceOrder updatedServiceOrder) {
        return serviceOrderRepository.findById(id)
                .map(order -> {
                    order.setVehicleId(updatedServiceOrder.getVehicleId());
                    order.setMechanicId(updatedServiceOrder.getMechanicId());
                    order.setLaborCost(updatedServiceOrder.getLaborCost());
                    order.setStatus(updatedServiceOrder.getStatus());

                    if (updatedServiceOrder.getPartIds() != null) {
                        order.setPartIds(updatedServiceOrder.getPartIds());
                    }
                    if (updatedServiceOrder.getRequiredServices() != null) {
                        order.setRequiredServices(updatedServiceOrder.getRequiredServices());
                    }
                    if (updatedServiceOrder.getCompletedServices() != null) {
                        order.setCompletedServices(updatedServiceOrder.getCompletedServices());
                    }

                    return serviceOrderRepository.save(order);
                })
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));
    }

    // 🗑️ Удалить заказ
    public boolean deleteServiceOrder(Long id) {
        if (serviceOrderRepository.existsById(id)) {
            serviceOrderRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // 🔍 Найти заказ по ID
    public ServiceOrder getServiceOrderById(Long id) {
        return serviceOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));
    }

    // 💰 Расчет общей стоимости заказа
    public double calculateTotalCost(Long orderId) {
        ServiceOrder order = getServiceOrderById(orderId);

        double partsCost = order.getPartIds().stream()
                .mapToDouble(partId -> partRepository.findById(partId)
                        .map(part -> part.getPrice())
                        .orElse(0.0))
                .sum();

        return partsCost + order.getLaborCost();
    }

    // ✅ Добавить выполненную работу в заказ
    public ServiceOrder addCompletedService(Long orderId, String service) {
        ServiceOrder order = getServiceOrderById(orderId);

        if (!order.getCompletedServices().contains(service)) {
            order.getCompletedServices().add(service);

            // Автоматически меняем статус на "IN_PROGRESS" если начали работы
            if ("CREATED".equals(order.getStatus())) {
                order.setStatus("IN_PROGRESS");
            }

            return serviceOrderRepository.save(order);
        }
        return order;
    }

    // 🚗 Закрыть заказ (только если все обязательные работы выполнены)
    public ServiceOrder completeOrder(Long orderId) {
        ServiceOrder order = getServiceOrderById(orderId);

        if (order.canBeCompleted()) {
            order.setStatus("COMPLETED");
            order.setCompletionDate(LocalDate.now());
            return serviceOrderRepository.save(order);
        }
        throw new IllegalStateException("Нельзя закрыть заказ - не все работы выполнены");
    }

    // 📊 Получить заказы по статусу
    public List<ServiceOrder> getOrdersByStatus(String status) {
        return serviceOrderRepository.findByStatus(status);
    }

    // 👨‍🔧 Получить заказы по механику
    public List<ServiceOrder> getOrdersByMechanic(Long mechanicId) {
        return serviceOrderRepository.findByMechanicId(mechanicId);
    }

    // 🔍 Проверить можно ли закрыть заказ
    public boolean canCompleteOrder(Long orderId) {
        ServiceOrder order = getServiceOrderById(orderId);
        return order.canBeCompleted();
    }

    // 📈 Получить статистику по заказам
    public String getOrderStatistics() {
        long created = serviceOrderRepository.countByStatus("CREATED");
        long inProgress = serviceOrderRepository.countByStatus("IN_PROGRESS");
        long completed = serviceOrderRepository.countByStatus("COMPLETED");

        return String.format("Заказы: CREATED=%d, IN_PROGRESS=%d, COMPLETED=%d",
                created, inProgress, completed);
    }
}