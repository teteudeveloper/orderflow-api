package com.orderflow.api.service;

import com.orderflow.api.exception.BusinessException;
import com.orderflow.api.exception.ResourceNotFoundException;
import com.orderflow.api.model.dto.CustomerRequestDTO;
import com.orderflow.api.model.dto.CustomerResponseDTO;
import com.orderflow.api.model.entity.Customer;
import com.orderflow.api.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Unit Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private CustomerRequestDTO validRequest;
    private Customer existingCustomer;

    @BeforeEach
    void setUp() {
        validRequest = CustomerRequestDTO.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("+5511999999999")
                .documentNumber("12345678901")
                .build();

        existingCustomer = Customer.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .phone("+5511999999999")
                .documentNumber("12345678901")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create customer successfully")
    void shouldCreateCustomerSuccessfully() {
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(customerRepository.findByDocumentNumber(anyString())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(existingCustomer);

        CustomerResponseDTO response = customerService.create(validRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");

        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when email already exists")
    void shouldThrowExceptionWhenEmailExists() {
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.of(existingCustomer));

        assertThatThrownBy(() -> customerService.create(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email already in use");

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when document number already exists")
    void shouldThrowExceptionWhenDocumentExists() {
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(customerRepository.findByDocumentNumber(anyString())).thenReturn(Optional.of(existingCustomer));

        assertThatThrownBy(() -> customerService.create(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Document number already in use");

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should find customer by id successfully")
    void shouldFindCustomerByIdSuccessfully() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existingCustomer));

        CustomerResponseDTO response = customerService.findById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John Doe");

        verify(customerRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when customer not found")
    void shouldThrowExceptionWhenCustomerNotFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found");

        verify(customerRepository).findById(999L);
    }

    @Test
    @DisplayName("Should find all customers with pagination")
    void shouldFindAllCustomersWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> customerPage = new PageImpl<>(List.of(existingCustomer));

        when(customerRepository.findAll(pageable)).thenReturn(customerPage);

        Page<CustomerResponseDTO> response = customerService.findAll(pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getName()).isEqualTo("John Doe");

        verify(customerRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should update customer successfully")
    void shouldUpdateCustomerSuccessfully() {
        CustomerRequestDTO updateRequest = CustomerRequestDTO.builder()
                .name("Jane Doe")
                .email("john@example.com")
                .phone("+5511988888888")
                .documentNumber("12345678901")
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.findByDocumentNumber(anyString())).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(existingCustomer);

        CustomerResponseDTO response = customerService.update(1L, updateRequest);

        assertThat(response).isNotNull();
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should delete customer successfully")
    void shouldDeleteCustomerSuccessfully() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(customerRepository).deleteById(1L);

        assertThatCode(() -> customerService.delete(1L))
                .doesNotThrowAnyException();

        verify(customerRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent customer")
    void shouldThrowExceptionWhenDeletingNonExistentCustomer() {
        when(customerRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> customerService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(customerRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should search customers by name")
    void shouldSearchCustomersByName() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> customerPage = new PageImpl<>(List.of(existingCustomer));

        when(customerRepository.findByNameContainingIgnoreCase("John", pageable))
                .thenReturn(customerPage);

        Page<CustomerResponseDTO> response = customerService.searchByName("John", pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getName()).contains("John");

        verify(customerRepository).findByNameContainingIgnoreCase("John", pageable);
    }
}