package com.orderflow.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String documentNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}