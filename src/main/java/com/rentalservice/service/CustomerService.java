package com.rentalservice.service;

import com.rentalservice.model.Customer;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerService {
    private final List<Customer> customers = new ArrayList<>();
    private Long nextId = 1L;

    // 📋 Получить всех клиентов
    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customers); // возвращаем копию для безопасности
    }

    // ➕ Создать нового клиента
    public Customer createCustomer(Customer customer) {
        customer.setId(nextId++);
        customers.add(customer);
        return customer;
    }

    // ✏️ Обновить данные клиента
    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        for (Customer customer : customers) {
            if (customer.getId().equals(id)) {
                customer.setName(updatedCustomer.getName());
                customer.setPhoneNumber(updatedCustomer.getPhoneNumber());
                customer.setEmail(updatedCustomer.getEmail());
                customer.setAddress(updatedCustomer.getAddress());
                return customer;
            }
        }
        return null; // клиент не найден
    }

    // 🗑️ Удалить клиента
    public boolean deleteCustomer(Long id) {
        return customers.removeIf(customer -> customer.getId().equals(id));
    }

    // 🔍 Найти клиента по ID
    public Customer getCustomerById(Long id) {
        return customers.stream()
                .filter(customer -> customer.getId().equals(id))
                .findFirst()
                .orElse(null); // возвращаем null если не найден
    }
}