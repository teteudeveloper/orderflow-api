# OrderFlow API

## Overview
**OrderFlow API** is a RESTful backend application for managing customers and orders in B2B environments. It is designed with a production-oriented mindset, emphasizing clean architecture, explicit business rules, and robustness expected from enterprise-grade systems.

The project demonstrates how to build a reliable API with clear separation of concerns, strong validation, and predictable behavior across different environments.

---

## Problem Statement
Many B2B backends suffer from structural and operational weaknesses such as:

- Fragile APIs with unclear responsibilities
- Scattered or implicit business rules
- Poor configuration management across environments
- Complex or unreliable local setup processes
- Order workflows without enforced lifecycle constraints

These issues lead to higher maintenance costs, increased bug rates, and unpredictable system behavior.

---

## Solution
OrderFlow API addresses these problems by providing:

- A clean, layered architecture aligned with enterprise standards
- Centralized and explicit business rules in the service layer
- Strong validation and consistency guarantees for core entities
- Enforced order lifecycle with invalid transitions blocked
- Clear and reproducible configuration for local, test, Docker, and production environments

The result is a backend system that is easy to reason about, extend, and operate.

---

## Project Purpose
The main goals of OrderFlow API are to:

- Demonstrate backend best practices in a realistic B2B domain
- Enforce business rules explicitly rather than implicitly
- Provide predictable API behavior through validation and lifecycle control
- Offer a clean and maintainable codebase suitable for production use

---

## Core Features

### Customer Management
- Create, update, and delete customers
- Strong data validation (email, document, required fields)
- Uniqueness constraints to prevent duplicated records
- Search by ID and name
- Paginated customer listing

---

### Order Management
- Create orders with multiple items
- Automatic subtotal and total price calculation
- Explicit order status lifecycle:
    - `CREATED → PROCESSING → COMPLETED`
- Invalid status transitions are explicitly blocked
- Filtering orders by customer and status

---

### Infrastructure and Reliability
- Health check endpoints via Spring Actuator
- Global exception handling with consistent error responses
- Unit and integration tests
- Application ready to run with or without Docker

---

## Architecture Overview
OrderFlow API follows a **layered architecture** commonly adopted in enterprise backend systems.

### Layers and Responsibilities
- **Controllers**  
  Expose REST endpoints and handle HTTP concerns.

- **Services**  
  Contain all business rules, validations, and lifecycle enforcement.

- **Repositories**  
  Handle data persistence using Spring Data JPA.

- **DTOs**  
  Isolate API contracts from internal domain entities, preventing leakage of persistence concerns.

This structure ensures clear responsibility boundaries and simplifies maintenance and testing.

---

## Technical Decisions and Trade-offs

### Why Spring Boot?
- Mature and widely adopted framework for enterprise backends
- Strong ecosystem for data access, validation, and testing
- Built-in support for production concerns (health checks, configuration, metrics)

**Trade-off:** Higher abstraction level compared to lightweight frameworks, accepted for stability and productivity.

---

### Why Layered Architecture?
- Clear separation of concerns
- Business rules centralized in the service layer
- Easier testing and long-term maintenance

**Trade-off:** Slightly more boilerplate, justified by clarity and scalability.

---

### Why JPA with PostgreSQL?
- Strong relational modeling
- Database portability
- Integration with Spring ecosystem

**Trade-off:** ORM abstraction overhead compared to raw SQL, accepted for consistency and maintainability.

---

### Why Explicit Order Lifecycle?
- Prevents invalid or inconsistent state transitions
- Makes business rules visible and enforceable
- Simplifies reasoning about order state

**Trade-off:** Reduced flexibility, accepted to guarantee correctness.

---

## Data Model Overview

### Customer
- Unique identifiers and validated attributes
- Used as the parent entity for orders

### Order
- Associated with a single customer
- Contains one or more items
- Status controlled by a strict lifecycle
- Pricing derived automatically from items

---

## Validation and Business Rules
- Input validation at API boundaries
- Business invariants enforced in the service layer
- Invalid operations fail fast with clear error messages

This approach ensures that invalid data never reaches the persistence layer.

---

## Testing Strategy
The project includes:

- Unit tests for business logic
- Integration tests for persistence and API behavior
- Isolated test environment using H2

The testing strategy prioritizes correctness of business rules and lifecycle enforcement.

---

## Roadmap
- Authentication and authorization
- Role-based access control
- Audit logging for order changes
- API documentation with OpenAPI / Swagger
- Event-driven extensions (outbox or messaging)

---

## License
This project is licensed under the MIT License.
