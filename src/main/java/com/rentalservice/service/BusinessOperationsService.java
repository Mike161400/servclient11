package com.rentalservice.service;

import com.rentalservice.model.*;
import com.rentalservice.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BusinessOperationsService {

    private final ServiceOrderRepository serviceOrderRepository;
    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final PartRepository partRepository;
    private final MechanicRepository mechanicRepository;

    public BusinessOperationsService(
            ServiceOrderRepository serviceOrderRepository,
            VehicleRepository vehicleRepository,
            CustomerRepository customerRepository,
            PartRepository partRepository,
            MechanicRepository mechanicRepository) {
        this.serviceOrderRepository = serviceOrderRepository;
        this.vehicleRepository = vehicleRepository;
        this.customerRepository = customerRepository;
        this.partRepository = partRepository;
        this.mechanicRepository = mechanicRepository;
    }

    // ==================== 5 БИЗНЕС-ОПЕРАЦИЙ ====================

    // 🚗 ОПЕРАЦИЯ 1: Создание полного заказа с транзакцией
    @Transactional
    public ServiceOrder createFullServiceOrder(Long vehicleId, Long mechanicId,
                                               List<String> requiredServices,
                                               List<Long> partIds) {

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден"));

        Mechanic mechanic = mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new IllegalArgumentException("Механик не найден"));

        // Проверяем доступность деталей
        List<Part> parts = partRepository.findAllById(partIds);
        if (parts.size() != partIds.size()) {
            throw new IllegalArgumentException("Некоторые детали не найдены");
        }

        // Проверяем количество на складе
        for (Part part : parts) {
            if (part.getQuantityInStock() <= 0) {
                throw new IllegalArgumentException("Деталь '" + part.getName() + "' отсутствует на складе");
            }
        }

        // Создаем заказ
        ServiceOrder order = new ServiceOrder();
        order.setVehicleId(vehicleId);
        order.setMechanicId(mechanicId);
        order.setCreationDate(LocalDate.now());
        order.setStatus("CREATED");
        order.setRequiredServices(requiredServices);
        order.setPartIds(partIds);

        // Рассчитываем стоимость работ (почасовая ставка механика × 2 часа на каждую услугу)
        double laborCost = mechanic.getHourlyRate() * 2 * requiredServices.size();
        order.setLaborCost(laborCost);

        // Сохраняем заказ
        ServiceOrder savedOrder = serviceOrderRepository.save(order);

        // Уменьшаем количество деталей на складе (в одной транзакции)
        for (Part part : parts) {
            part.setQuantityInStock(part.getQuantityInStock() - 1);
            partRepository.save(part);
        }

        return savedOrder;
    }

    // 💰 ОПЕРАЦИЯ 2: Генерация счета для клиента
    public Map<String, Object> generateInvoice(Long orderId) {
        ServiceOrder order = serviceOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

        Vehicle vehicle = vehicleRepository.findById(order.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден"));

        Customer customer = customerRepository.findById(vehicle.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

        List<Part> parts = partRepository.findAllById(order.getPartIds());

        double partsCost = parts.stream()
                .mapToDouble(Part::getPrice)
                .sum();

        double totalCost = partsCost + order.getLaborCost();

        Map<String, Object> invoice = new HashMap<>();
        invoice.put("invoiceNumber", "INV-" + LocalDateTime.now().getYear() + "-" + orderId);
        invoice.put("customerName", customer.getName());
        invoice.put("customerPhone", customer.getPhoneNumber());
        invoice.put("vehicle", vehicle.getBrand() + " " + vehicle.getModel() + " (" + vehicle.getLicensePlate() + ")");
        invoice.put("vin", vehicle.getVin());
        invoice.put("orderId", orderId);
        invoice.put("orderDate", order.getCreationDate());
        invoice.put("services", order.getRequiredServices());
        invoice.put("parts", parts.stream().map(p -> Map.of(
                "name", p.getName(),
                "partNumber", p.getPartNumber(),
                "price", p.getPrice()
        )).collect(Collectors.toList()));
        invoice.put("laborCost", order.getLaborCost());
        invoice.put("partsCost", partsCost);
        invoice.put("totalCost", totalCost);
        invoice.put("tax", totalCost * 0.20); // НДС 20%
        invoice.put("totalWithTax", totalCost * 1.20);
        invoice.put("generatedDate", LocalDate.now());

        return invoice;
    }

    // 📊 ОПЕРАЦИЯ 3: Отчет по работе механиков за период
    public Map<String, Object> getMechanicPerformanceReport(LocalDate startDate, LocalDate endDate) {
        List<ServiceOrder> orders = serviceOrderRepository
                .findByCreationDateBetween(startDate, endDate);

        Map<Long, Map<String, Object>> mechanicStats = new HashMap<>();

        for (ServiceOrder order : orders) {
            Mechanic mechanic = mechanicRepository.findById(order.getMechanicId()).orElse(null);

            if (mechanic != null) {
                mechanicStats.putIfAbsent(mechanic.getId(), new HashMap<>());
                Map<String, Object> stats = mechanicStats.get(mechanic.getId());

                stats.put("mechanicName", mechanic.getName());
                stats.put("specialization", mechanic.getSpecialization());
                stats.put("totalOrders", (int) stats.getOrDefault("totalOrders", 0) + 1);
                stats.put("totalRevenue", (double) stats.getOrDefault("totalRevenue", 0.0)
                        + calculateOrderTotal(order));

                // Статистика по статусам
                if ("COMPLETED".equals(order.getStatus())) {
                    stats.put("completedOrders", (int) stats.getOrDefault("completedOrders", 0) + 1);
                } else if ("IN_PROGRESS".equals(order.getStatus())) {
                    stats.put("inProgressOrders", (int) stats.getOrDefault("inProgressOrders", 0) + 1);
                }
            }
        }

        Map<String, Object> report = new HashMap<>();
        report.put("period", startDate + " - " + endDate);
        report.put("totalOrders", orders.size());
        report.put("totalRevenue", orders.stream().mapToDouble(this::calculateOrderTotal).sum());
        report.put("mechanicPerformance", mechanicStats);

        return report;
    }

    // 🚨 ОПЕРАЦИЯ 4: Проверка и автоматический заказ недостающих деталей
    @Transactional
    public List<Map<String, Object>> checkAndOrderLowStockParts(int minThreshold) {
        List<Part> lowStockParts = partRepository.findByQuantityInStockLessThan(minThreshold);

        return lowStockParts.stream().map(part -> {
            Map<String, Object> orderInfo = new HashMap<>();
            orderInfo.put("partId", part.getId());
            orderInfo.put("partName", part.getName());
            orderInfo.put("partNumber", part.getPartNumber());
            orderInfo.put("currentStock", part.getQuantityInStock());
            orderInfo.put("minThreshold", minThreshold);

            // Рассчитываем сколько нужно заказать
            int orderQuantity = Math.max(20, minThreshold * 3 - part.getQuantityInStock());
            orderInfo.put("orderQuantity", orderQuantity);
            orderInfo.put("unitPrice", part.getPrice());
            orderInfo.put("estimatedCost", part.getPrice() * orderQuantity);
            orderInfo.put("orderDate", LocalDate.now());
            orderInfo.put("expectedDelivery", LocalDate.now().plusDays(7));
            orderInfo.put("supplier", part.getSupplier());

            // В реальном приложении здесь был бы вызов API поставщика
            // Для демо просто обновим количество
            part.setQuantityInStock(part.getQuantityInStock() + orderQuantity);
            partRepository.save(part);

            orderInfo.put("newStock", part.getQuantityInStock());
            orderInfo.put("status", "ORDERED");

            return orderInfo;
        }).collect(Collectors.toList());
    }

    // 📝 ОПЕРАЦИЯ 5: Быстрое оформление ТО (технического обслуживания) - пакетная услуга
    @Transactional
    public ServiceOrder quickMaintenance(Long vehicleId, String maintenanceType) {
        // Находим свободного механика
        List<Mechanic> availableMechanics = mechanicRepository.findByActiveTrue();
        if (availableMechanics.isEmpty()) {
            throw new IllegalStateException("Нет доступных механиков");
        }

        // Стандартные пакеты ТО
        Map<String, MaintenancePackage> maintenancePackages = Map.of(
                "BASIC", new MaintenancePackage(
                        List.of("Замена масла", "Замена масляного фильтра", "Проверка жидкостей"),
                        List.of(1L, 8L), // Масляный фильтр + Масло
                        2.0 // 2 часа работы
                ),
                "STANDARD", new MaintenancePackage(
                        List.of("Замена масла", "Замена фильтров", "Проверка тормозов",
                                "Диагностика подвески", "Замена воздушного фильтра"),
                        List.of(1L, 2L, 8L), // Масляный фильтр + Воздушный фильтр + Масло
                        3.5 // 3.5 часа работы
                ),
                "PREMIUM", new MaintenancePackage(
                        List.of("Полная диагностика", "Замена всех фильтров", "Проверка электроники",
                                "Регулировка фар", "Химчистка салона"),
                        List.of(1L, 2L, 3L, 8L, 9L), // Все фильтры + жидкости
                        5.0 // 5 часов работы
                )
        );

        MaintenancePackage packageInfo = maintenancePackages.get(maintenanceType.toUpperCase());
        if (packageInfo == null) {
            throw new IllegalArgumentException("Неизвестный тип ТО: " + maintenanceType);
        }

        Mechanic mechanic = availableMechanics.get(0);

        // Создаем заказ
        ServiceOrder order = new ServiceOrder();
        order.setVehicleId(vehicleId);
        order.setMechanicId(mechanic.getId());
        order.setCreationDate(LocalDate.now());
        order.setStatus("IN_PROGRESS");
        order.setRequiredServices(packageInfo.services);
        order.setPartIds(packageInfo.partIds);

        // Стоимость работ
        double laborCost = mechanic.getHourlyRate() * packageInfo.hours;
        order.setLaborCost(laborCost);

        ServiceOrder savedOrder = serviceOrderRepository.save(order);

        // Уменьшаем количество деталей
        List<Part> parts = partRepository.findAllById(packageInfo.partIds);
        for (Part part : parts) {
            if (part.getQuantityInStock() > 0) {
                part.setQuantityInStock(part.getQuantityInStock() - 1);
                partRepository.save(part);
            }
        }

        return savedOrder;
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private double calculateOrderTotal(ServiceOrder order) {
        List<Part> parts = partRepository.findAllById(order.getPartIds());
        double partsCost = parts.stream()
                .mapToDouble(Part::getPrice)
                .sum();
        return partsCost + order.getLaborCost();
    }

    // Вспомогательный класс для пакетов ТО
    private static class MaintenancePackage {
        List<String> services;
        List<Long> partIds;
        double hours;

        MaintenancePackage(List<String> services, List<Long> partIds, double hours) {
            this.services = services;
            this.partIds = partIds;
            this.hours = hours;
        }
    }
}