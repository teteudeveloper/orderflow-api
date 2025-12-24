package com.orderflow.api.service;

import com.orderflow.api.exception.BusinessException;
import com.orderflow.api.exception.ResourceNotFoundException;
import com.orderflow.api.model.dto.OrderItemRequestDTO;
import com.orderflow.api.model.dto.OrderRequestDTO;
import com.orderflow.api.model.dto.OrderResponseDTO;
import com.orderflow.api.model.entity.Customer;
import com.orderflow.api.model.entity.Order;
import com.orderflow.api.model.entity.OrderItem;
import com.orderflow.api.model.entity.OrderStatus;
import com.orderflow.api.repository.CustomerRepository;
import com.orderflow.api.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private OrderService orderService;

    private Customer testCustomer;
    private Order testOrder;
    private OrderRequestDTO validOrderRequest;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .phone("+5511999999999")
                .documentNumber("12345678901")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        OrderItem item1 = OrderItem.builder()
                .id(1L)
                .productName("Product A")
                .quantity(2)
                .unitPrice(new BigDecimal("50.00"))
                .subtotal(new BigDecimal("100.00"))
                .build();

        OrderItem item2 = OrderItem.builder()
                .id(2L)
                .productName("Product B")
                .quantity(1)
                .unitPrice(new BigDecimal("150.00"))
                .subtotal(new BigDecimal("150.00"))
                .build();

        testOrder = Order.builder()
                .id(1L)
                .customer(testCustomer)
                .items(new ArrayList<>(List.of(item1, item2)))
                .totalAmount(new BigDecimal("250.00"))
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        item1.setOrder(testOrder);
        item2.setOrder(testOrder);

        OrderItemRequestDTO itemRequest1 = OrderItemRequestDTO.builder()
                .productName("Product A")
                .quantity(2)
                .unitPrice(new BigDecimal("50.00"))
                .build();

        OrderItemRequestDTO itemRequest2 = OrderItemRequestDTO.builder()
                .productName("Product B")
                .quantity(1)
                .unitPrice(new BigDecimal("150.00"))
                .build();

        validOrderRequest = OrderRequestDTO.builder()
                .customerId(1L)
                .items(List.of(itemRequest1, itemRequest2))
                .build();
    }

    @Test
    @DisplayName("Should create order successfully and calculate total amount")
    void shouldCreateOrderSuccessfully() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        OrderResponseDTO response = orderService.create(validOrderRequest);

        assertThat(response).isNotNull();
        assertThat(response.getCustomerId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(response.getTotalAmount()).isEqualTo(new BigDecimal("250.00"));
        assertThat(response.getItems()).hasSize(2);

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when customer not found on order creation")
    void shouldThrowExceptionWhenCustomerNotFoundOnCreate() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        OrderRequestDTO invalidRequest = OrderRequestDTO.builder()
                .customerId(999L)
                .items(validOrderRequest.getItems())
                .build();

        assertThatThrownBy(() -> orderService.create(invalidRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should calculate total amount correctly with multiple items")
    void shouldCalculateTotalAmountCorrectly() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        OrderResponseDTO response = orderService.create(validOrderRequest);

        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("250.00"));

        BigDecimal item1Total = response.getItems().get(0).getSubtotal();
        BigDecimal item2Total = response.getItems().get(1).getSubtotal();

        assertThat(item1Total).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(item2Total).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Should find order by id successfully")
    void shouldFindOrderByIdSuccessfully() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        OrderResponseDTO response = orderService.findById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCustomerName()).isEqualTo("John Doe");

        verify(orderRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void shouldThrowExceptionWhenOrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    @DisplayName("Should update order status from CREATED to PROCESSING")
    void shouldUpdateStatusFromCreatedToProcessing() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        OrderResponseDTO response = orderService.updateStatus(1L, OrderStatus.PROCESSING);

        assertThat(response).isNotNull();
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should update order status from PROCESSING to COMPLETED")
    void shouldUpdateStatusFromProcessingToCompleted() {
        testOrder.setStatus(OrderStatus.PROCESSING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        OrderResponseDTO response = orderService.updateStatus(1L, OrderStatus.COMPLETED);

        assertThat(response).isNotNull();
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when trying to change status of completed order")
    void shouldThrowExceptionWhenChangingCompletedOrderStatus() {
        testOrder.setStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.PROCESSING))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot change status of completed order");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when trying to go from CREATED directly to COMPLETED")
    void shouldThrowExceptionWhenSkippingProcessingStatus() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.COMPLETED))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Order must be in PROCESSING status before completion");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should delete order successfully")
    void shouldDeleteOrderSuccessfully() {
        when(orderRepository.existsById(1L)).thenReturn(true);
        doNothing().when(orderRepository).deleteById(1L);

        assertThatCode(() -> orderService.delete(1L))
                .doesNotThrowAnyException();

        verify(orderRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent order")
    void shouldThrowExceptionWhenDeletingNonExistentOrder() {
        when(orderRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> orderService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(orderRepository, never()).deleteById(anyLong());
    }
}