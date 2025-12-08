package com.rentalservice.controller;

import com.rentalservice.model.Customer;
import com.rentalservice.service.CustomerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    // 💉 Конструктор с внедрением зависимости (Dependency Injection)
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // 📋 GET /customers - получить всех клиентов
    @GetMapping
    public List<Customer> getAll() {
        return customerService.getAllCustomers();
    }

    // 🔍 GET /customers/{id} - получить клиента по ID
    @GetMapping("/{id}")
    public Customer getById(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }

    // ➕ POST /customers - создать нового клиента
    @PostMapping
    public Customer create(@RequestBody Customer customer) {
        return customerService.createCustomer(customer);
    }

    // ✏️ PUT /customers/{id} - обновить данные клиента
    @PutMapping("/{id}")
    public Customer update(@PathVariable Long id, @RequestBody Customer customer) {
        return customerService.updateCustomer(id, customer);
    }

    // 🗑️ DELETE /customers/{id} - удалить клиента
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return customerService.deleteCustomer(id);
    }
}