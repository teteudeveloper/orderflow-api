package com.orderflow.api.controller;

import com.orderflow.api.model.dto.OrderRequestDTO;
import com.orderflow.api.model.dto.OrderResponseDTO;
import com.orderflow.api.model.entity.OrderStatus;
import com.orderflow.api.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(@Valid @RequestBody OrderRequestDTO request) {
        OrderResponseDTO response = orderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> findById(@PathVariable Long id) {
        OrderResponseDTO response = orderService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> findAll(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<OrderResponseDTO> response = orderService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<OrderResponseDTO>> findByCustomerId(
            @PathVariable Long customerId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<OrderResponseDTO> response = orderService.findByCustomerId(customerId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<OrderResponseDTO>> findByStatus(
            @PathVariable OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<OrderResponseDTO> response = orderService.findByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        OrderResponseDTO response = orderService.updateStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}