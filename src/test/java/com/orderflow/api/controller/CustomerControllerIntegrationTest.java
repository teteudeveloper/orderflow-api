package com.orderflow.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderflow.api.model.dto.CustomerRequestDTO;
import com.orderflow.api.model.entity.Customer;
import com.orderflow.api.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CustomerController Integration Tests")
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    private CustomerRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();

        validRequest = CustomerRequestDTO.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("+5511999999999")
                .documentNumber("12345678901")
                .build();
    }

    @Test
    @DisplayName("POST /api/customers - Should create customer successfully")
    void shouldCreateCustomerSuccessfully() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.phone").value("+5511999999999"))
                .andExpect(jsonPath("$.documentNumber").value("12345678901"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("POST /api/customers - Should return 400 when name is blank")
    void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
        validRequest.setName("");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("name")));
    }

    @Test
    @DisplayName("POST /api/customers - Should return 400 when email is invalid")
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        validRequest.setEmail("invalid-email");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("email")));
    }

    @Test
    @DisplayName("POST /api/customers - Should return 400 when email already exists")
    void shouldReturnBadRequestWhenEmailExists() throws Exception {
        Customer existing = Customer.builder()
                .name("Jane Doe")
                .email("john@example.com")
                .phone("+5511988888888")
                .documentNumber("98765432100")
                .build();
        customerRepository.save(existing);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Email already in use")));
    }

    @Test
    @DisplayName("GET /api/customers/{id} - Should return customer when found")
    void shouldReturnCustomerWhenFound() throws Exception {
        Customer saved = customerRepository.save(Customer.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("+5511999999999")
                .documentNumber("12345678901")
                .build());

        mockMvc.perform(get("/api/customers/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @DisplayName("GET /api/customers/{id} - Should return 404 when customer not found")
    void shouldReturn404WhenCustomerNotFound() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Customer not found")));
    }

    @Test
    @DisplayName("GET /api/customers - Should return paginated customers")
    void shouldReturnPaginatedCustomers() throws Exception {
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

        mockMvc.perform(get("/api/customers")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    @DisplayName("GET /api/customers/search - Should search customers by name")
    void shouldSearchCustomersByName() throws Exception {
        customerRepository.save(Customer.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("+5511999999999")
                .documentNumber("12345678901")
                .build());

        customerRepository.save(Customer.builder()
                .name("Jane Smith")
                .email("jane@example.com")
                .phone("+5511988888888")
                .documentNumber("98765432100")
                .build());

        mockMvc.perform(get("/api/customers/search")
                        .param("name", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("John Doe"));
    }

    @Test
    @DisplayName("PUT /api/customers/{id} - Should update customer successfully")
    void shouldUpdateCustomerSuccessfully() throws Exception {
        Customer saved = customerRepository.save(Customer.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("+5511999999999")
                .documentNumber("12345678901")
                .build());

        CustomerRequestDTO updateRequest = CustomerRequestDTO.builder()
                .name("John Updated")
                .email("john@example.com")
                .phone("+5511977777777")
                .documentNumber("12345678901")
                .build();

        mockMvc.perform(put("/api/customers/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.phone").value("+5511977777777"));
    }

    @Test
    @DisplayName("DELETE /api/customers/{id} - Should delete customer successfully")
    void shouldDeleteCustomerSuccessfully() throws Exception {
        Customer saved = customerRepository.save(Customer.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("+5511999999999")
                .documentNumber("12345678901")
                .build());

        mockMvc.perform(delete("/api/customers/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/customers/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/customers/{id} - Should return 404 when deleting non-existent customer")
    void shouldReturn404WhenDeletingNonExistentCustomer() throws Exception {
        mockMvc.perform(delete("/api/customers/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}