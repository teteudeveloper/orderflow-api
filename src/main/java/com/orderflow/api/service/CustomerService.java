package com.orderflow.api.service;

import com.orderflow.api.exception.BusinessException;
import com.orderflow.api.exception.ResourceNotFoundException;
import com.orderflow.api.model.dto.CustomerRequestDTO;
import com.orderflow.api.model.dto.CustomerResponseDTO;
import com.orderflow.api.model.entity.Customer;
import com.orderflow.api.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponseDTO create(CustomerRequestDTO request) {
        validateUniqueConstraints(request.getEmail(), request.getDocumentNumber(), null);

        Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .documentNumber(request.getDocumentNumber())
                .build();

        Customer saved = customerRepository.save(customer);
        return mapToResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public CustomerResponseDTO findById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        return mapToResponseDTO(customer);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponseDTO> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponseDTO> searchByName(String name, Pageable pageable) {
        return customerRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional
    public CustomerResponseDTO update(Long id, CustomerRequestDTO request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));

        validateUniqueConstraints(request.getEmail(), request.getDocumentNumber(), id);

        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setDocumentNumber(request.getDocumentNumber());

        Customer updated = customerRepository.save(customer);
        return mapToResponseDTO(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer", id);
        }
        customerRepository.deleteById(id);
    }

    private void validateUniqueConstraints(String email, String documentNumber, Long excludeId) {

        customerRepository.findByEmail(email).ifPresent(existing -> {
            if (excludeId == null || !existing.getId().equals(excludeId)) {
                throw new BusinessException("Email already in use");
            }
        });

        customerRepository.findByDocumentNumber(documentNumber).ifPresent(existing -> {
            if (excludeId == null || !existing.getId().equals(excludeId)) {
                throw new BusinessException("Document number already in use");
            }
        });
    }

    private CustomerResponseDTO mapToResponseDTO(Customer customer) {
        return CustomerResponseDTO.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .documentNumber(customer.getDocumentNumber())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
