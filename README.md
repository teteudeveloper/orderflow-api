# OrderFlow API

OrderFlow API is a REST API for managing customers and orders in B2B environments, built with a production-oriented mindset and strong backend best practices.

The project focuses on clean architecture, explicit business rules, and an easy setup experience for anyone cloning the repository.

---

## 🎯 Project Purpose

Many B2B systems struggle with:
- Fragile APIs
- Poor configuration management
- Scattered business rules
- Difficult local setup

OrderFlow API addresses these issues by providing:
- Customer management with strong validation and uniqueness rules
- Order creation with automatic price calculation
- Explicit order lifecycle control
- Clear configuration for **local**, **docker**, **test**, and **production** environments

---

## 🧩 Core Features

### Customers
- Create, update, and delete customers
- Data validation (email, document, etc.)
- Search by ID, name, and paginated listing

### Orders
- Create orders with items
- Automatic subtotal and total calculation
- Status lifecycle:
    - CREATED → PROCESSING → COMPLETED
    - Invalid transitions are blocked
- Filters by customer and status

### Infrastructure
- Health check via Spring Actuator
- Global exception handling
- Unit and integration tests
- Ready to run with or without Docker

---

## 🛠️ Tech Stack

- **Java 17+**
- **Spring Boot 3**
- **Spring Data JPA**
- **PostgreSQL**
- **H2 (tests)**
- **JUnit 5 / Mockito**
- **Docker & Docker Compose**
- **Maven**

---

## 🏗️ Architecture

Layered architecture following enterprise standards:

- Controllers expose REST endpoints
- Services hold business rules
- Repositories handle persistence via JPA
- DTOs isolate API contracts from entities

---