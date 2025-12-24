package com.orderflow.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderflow.api.model.dto.OrderItemRequestDTO;
import com.orderflow.api.model.dto.OrderRequestDTO;
import com.orderflow.api.model.entity.Customer;
import com.orderflow.api.model.entity.Order;
import com.orderflow.api.model.entity.OrderStatus;
import com.orderflow.api.repository.CustomerRepository;
import com.orderflow.api.repository.OrderRepository;
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

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("OrderController Integration Tests")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Customer customer;
    private OrderRequestDTO validOrderRequest;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        customerRepository.deleteAll();

        customer = customerRepository.save(Customer.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("+5511999999999")
                .documentNumber("12345678901")
                .build());

        validOrderRequest = OrderRequestDTO.builder()
                .customerId(customer.getId())
                .items(List.of(
                        OrderItemRequestDTO.builder()
                                .productName("Product A")
                                .quantity(2)
                                .unitPrice(new BigDecimal("50.00"))
                                .build()
                ))
                .build();
    }

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(100.00))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void shouldReturn404WhenCustomerDoesNotExist() throws Exception {
        validOrderRequest.setCustomerId(999L);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenItemsListIsEmpty() throws Exception {
        validOrderRequest.setItems(List.of());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenQuantityIsInvalid() throws Exception {
        validOrderRequest.setItems(List.of(
                OrderItemRequestDTO.builder()
                        .productName("Product A")
                        .quantity(0)
                        .unitPrice(new BigDecimal("50.00"))
                        .build()
        ));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFindOrderById() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .customer(customer)
                .status(OrderStatus.CREATED)
                .totalAmount(BigDecimal.ZERO)
                .build());

        mockMvc.perform(get("/api/orders/{id}", order.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenOrderNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnPaginatedOrders() throws Exception {
        orderRepository.save(Order.builder()
                .customer(customer)
                .status(OrderStatus.CREATED)
                .totalAmount(BigDecimal.ZERO)
                .build());

        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldFilterOrdersByCustomer() throws Exception {
        orderRepository.save(Order.builder()
                .customer(customer)
                .status(OrderStatus.CREATED)
                .totalAmount(BigDecimal.ZERO)
                .build());

        mockMvc.perform(get("/api/orders")
                        .param("customerId", customer.getId().toString()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFilterOrdersByStatus() throws Exception {
        orderRepository.save(Order.builder()
                .customer(customer)
                .status(OrderStatus.CREATED)
                .totalAmount(BigDecimal.ZERO)
                .build());

        mockMvc.perform(get("/api/orders")
                        .param("status", "CREATED"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateStatusToProcessing() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .customer(customer)
                .status(OrderStatus.CREATED)
                .totalAmount(BigDecimal.ZERO)
                .build());

        mockMvc.perform(patch("/api/orders/{id}/status", order.getId())
                        .param("status", "PROCESSING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    void shouldNotSkipProcessing() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .customer(customer)
                .status(OrderStatus.CREATED)
                .totalAmount(BigDecimal.ZERO)
                .build());

        mockMvc.perform(patch("/api/orders/{id}/status", order.getId())
                        .param("status", "COMPLETED"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotChangeCompletedOrder() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .customer(customer)
                .status(OrderStatus.COMPLETED)
                .totalAmount(BigDecimal.ZERO)
                .build());

        mockMvc.perform(patch("/api/orders/{id}/status", order.getId())
                        .param("status", "PROCESSING"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteOrderSuccessfully() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .customer(customer)
                .status(OrderStatus.CREATED)
                .totalAmount(BigDecimal.ZERO)
                .build());

        mockMvc.perform(delete("/api/orders/{id}", order.getId()))
                .andExpect(status().isNoContent());
    }
}
