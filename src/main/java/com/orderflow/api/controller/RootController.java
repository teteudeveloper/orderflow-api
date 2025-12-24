package com.orderflow.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
                "name", "OrderFlow API",
                "status", "running",
                "health", "/actuator/health",
                "customers", "/api/customers",
                "orders", "/api/orders"
        );
    }
}
