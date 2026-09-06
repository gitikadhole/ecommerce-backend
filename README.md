# E-Commerce Backend Platform

A full-stack e-commerce backend built with **Spring Boot 4.1.1**, covering authentication, product catalog, cart, inventory-safe checkout, order management, payments, and event-driven notifications.

## Overview

This project simulates a real-world e-commerce backend, built to demonstrate production-grade backend engineering practices. 

## Tech Stack

| Layer | Technology |
|---|---|
| Language / Framework | Java 21, Spring Boot 4.1.1 |
| Persistence | Spring Data JPA, MySQL |
| Security | Spring Security, JWT (access + refresh tokens) |
| Caching | Redis |
| Async Events | Spring Application Events (`@EventListener` / `@Async`) |
| API Docs | springdoc-openapi (Swagger UI) |
| Testing | JUnit 5, Mockito, Testcontainers |
| Build Tool | Maven |

## Architecture

- Monolith design with clearly separated layers (`controller` → `service` → `repository`)
- Domain entities: `User`, `Product`, `Category`, `Cart`, `CartItem`, `Order`, `OrderItem`, `Payment`, `Inventory`.
- Order lifecycle modeled as an explicit state machine: `PLACED → CONFIRMED → SHIPPED → DELIVERED`, with guarded transitions to prevent invalid state changes.

## Core Modules

- **User Service** — registration, login, JWT auth, role-based access (`CUSTOMER` / `ADMIN`)
- **Product Catalog** — categories, products, search, filtering, pagination, Redis-cached listings
- **Cart Service** — add/remove/update items, one active cart per user, stock-validated updates
- **Order Service** — checkout, order creation, order status workflow
- **Inventory Service** — stock tracking with **pessimistic locking** to prevent overselling under concurrent requests
- **Payment Integration** — Razorpay mock integration with webhook-based confirmation
- **Notification Service** — async order-event notifications via Spring's in-process event publishing (`ApplicationEventPublisher`)

## API Documentation

API docs are available via Swagger UI once the app is running:

```
http://localhost:8080/swagger-ui.html
```

## Getting Started

### Prerequisites
- Java 21+
- Maven
- MySql

## Project Structure

```
src/main/java/com/yourorg/ecommerce
├── config/         # Security, Swagger, Redis, async config
├── controller/      # REST controllers
├── dto/              # Request/response DTOs
├── entity/           # JPA entities
├── exception/       # Global exception handling
├── repository/       # Spring Data JPA repositories
├── security/         # JWT filter, auth providers
└── service/           # Business logic
```
