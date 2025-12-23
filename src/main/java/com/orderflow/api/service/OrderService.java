package com.orderflow.api.service;

import com.orderflow.api.exception.BusinessException;
import com.orderflow.api.exception.ResourceNotFoundException;
import com.orderflow.api.model.dto.OrderItemRequestDTO;
import com.orderflow.api.model.dto.OrderItemResponseDTO;
import com.orderflow.api.model.dto.OrderRequestDTO;
import com.orderflow.api.model.dto.OrderResponseDTO;
import com.orderflow.api.model.entity.Customer;
import com.orderflow.api.model.entity.Order;
import com.orderflow.api.model.entity.OrderItem;
import com.orderflow.api.model.entity.OrderStatus;
import com.orderflow.api.repository.CustomerRepository;
import com.orderflow.api.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public OrderResponseDTO create(OrderRequestDTO request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));

        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.CREATED)
                .totalAmount(BigDecimal.ZERO)
                .build();

        for (OrderItemRequestDTO itemDTO : request.getItems()) {
            OrderItem item = OrderItem.builder()
                    .productName(itemDTO.getProductName())
                    .quantity(itemDTO.getQuantity())
                    .unitPrice(itemDTO.getUnitPrice())
                    .build();
            item.calculateSubtotal();
            order.addItem(item);
        }

        order.calculateTotalAmount();
        Order saved = orderRepository.save(order);
        return mapToResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        return mapToResponseDTO(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> findByCustomerId(Long customerId, Pageable pageable) {
        return orderRepository.findByCustomerId(customerId, pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> findByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional
    public OrderResponseDTO updateStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);

        Order updated = orderRepository.save(order);
        return mapToResponseDTO(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order", id);
        }
        orderRepository.deleteById(id);
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == OrderStatus.COMPLETED) {
            throw new BusinessException("Cannot change status of completed order");
        }

        if (currentStatus == OrderStatus.CREATED && newStatus == OrderStatus.COMPLETED) {
            throw new BusinessException("Order must be in PROCESSING status before completion");
        }
    }

    private OrderResponseDTO mapToResponseDTO(Order order) {
        List<OrderItemResponseDTO> itemDTOs = order.getItems().stream()
                .map(item -> OrderItemResponseDTO.builder()
                        .id(item.getId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponseDTO.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getName())
                .items(itemDTOs)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}