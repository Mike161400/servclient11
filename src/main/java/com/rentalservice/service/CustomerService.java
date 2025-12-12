package com.rentalservice.service;

import com.rentalservice.model.Customer;
import com.rentalservice.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer createCustomer(Customer customer) {
        // Проверяем уникальность email и телефона
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new IllegalArgumentException("Email уже используется");
        }
        if (customerRepository.existsByPhoneNumber(customer.getPhoneNumber())) {
            throw new IllegalArgumentException("Номер телефона уже используется");
        }

        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        return customerRepository.findById(id)
                .map(customer -> {
                    // Проверяем уникальность email
                    if (!customer.getEmail().equals(updatedCustomer.getEmail())
                            && customerRepository.existsByEmail(updatedCustomer.getEmail())) {
                        throw new IllegalArgumentException("Email уже используется");
                    }

                    // Проверяем уникальность телефона
                    if (!customer.getPhoneNumber().equals(updatedCustomer.getPhoneNumber())
                            && customerRepository.existsByPhoneNumber(updatedCustomer.getPhoneNumber())) {
                        throw new IllegalArgumentException("Номер телефона уже используется");
                    }

                    customer.setName(updatedCustomer.getName());
                    customer.setPhoneNumber(updatedCustomer.getPhoneNumber());
                    customer.setEmail(updatedCustomer.getEmail());
                    customer.setAddress(updatedCustomer.getAddress());

                    return customerRepository.save(customer);
                })
                .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));
    }

    public boolean deleteCustomer(Long id) {
        if (customerRepository.existsById(id)) {
            customerRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));
    }
}