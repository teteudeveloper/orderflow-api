package com.orderflow.api.repository;

import com.orderflow.api.model.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CustomerRepository Integration Tests")
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save customer successfully")
    void shouldSaveCustomerSuccessfully() {
        Customer customer = Customer.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("+5511999999999")
                .documentNumber("12345678901")
                .build();

        Customer saved = customerRepository.save(customer);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("John Doe");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find customer by email")
    void shouldFindCustomerByEmail() {
        Customer customer = Customer.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("+5511999999999")
                .documentNumber("12345678901")
                .build();
        customerRepository.save(customer);

        Optional<Customer> found = customerRepository.findByEmail("john@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should find customer by document number")
    void shouldFindCustomerByDocumentNumber() {
        Customer customer = Customer.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("+5511999999999")
                .documentNumber("12345678901")
                .build();
        customerRepository.save(customer);

        Optional<Customer> found = customerRepository.findByDocumentNumber("12345678901");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("Should check if email exists")
    void shouldCheckIfEmailExists() {
        Customer customer = Customer.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("+5511999999999")
                .documentNumber("12345678901")
                .build();
        customerRepository.save(customer);

        boolean exists = customerRepository.existsByEmail("john@example.com");
        boolean notExists = customerRepository.existsByEmail("other@example.com");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should check if document number exists")
    void shouldCheckIfDocumentNumberExists() {
        Customer customer = Customer.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("+5511999999999")
                .documentNumber("12345678901")
                .build();
        customerRepository.save(customer);

        boolean exists = customerRepository.existsByDocumentNumber("12345678901");
        boolean notExists = customerRepository.existsByDocumentNumber("99999999999");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should find customers by name containing (case insensitive)")
    void shouldFindCustomersByNameContaining() {
        customerRepository.save(Customer.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("+5511999999999")
                .documentNumber("12345678901")
                .build());

        customerRepository.save(Customer.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .phone("+5511988888888")
                .documentNumber("98765432100")
                .build());

        customerRepository.save(Customer.builder()
                .name("Bob Smith")
                .email("bob@example.com")
                .phone("+5511977777777")
                .documentNumber("11111111111")
                .build());

        Page<Customer> doePage = customerRepository.findByNameContainingIgnoreCase("doe", PageRequest.of(0, 10));
        Page<Customer> johnPage = customerRepository.findByNameContainingIgnoreCase("JOHN", PageRequest.of(0, 10));

        assertThat(doePage.getContent()).hasSize(2);
        assertThat(johnPage.getContent()).hasSize(1);
        assertThat(johnPage.getContent().get(0).getName()).isEqualTo("John Doe");
    }
}