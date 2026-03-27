# Shop Management System

A multi-tenant REST API for retail operations built with Spring Boot 3.4.4. The system provides shop-scoped inventory management, order processing with asynchronous stock reconciliation, Redis-backed caching, JWT-based RBAC, rate limiting, and a full audit trail.

## Overview

Each registered shop gets its own isolated data space. A shop registration creates both the shop entity and its first ADMIN user in a single step. All subsequent API calls are scoped to the authenticated user's shop via a `shopId` claim embedded in the JWT.

## Key Features

- Multi-tenant architecture with shop-scoped data isolation enforced at the query level
- Product inventory CRUD with shop-level SKU uniqueness and optimistic locking
- Redis-backed caching for product lookups by ID and SKU (10-minute TTL, stampede protection via `sync=true`)
- Asynchronous stock reconciliation using Spring Events and a dedicated configurable thread pool
- Two-phase stock handling: soft check on order creation, hard deduction during async reconciliation
- Order status lifecycle: `PENDING` -> `COMPLETED` / `FAILED` with reconciliation timestamps and failure reasons
- JWT authentication with permission-based RBAC enforced via `@PreAuthorize` method-level security
- Bucket4j rate limiting: 500 req/s for authenticated users, 100 req/s for unauthenticated IPs
- Async audit logging for order creation and stock reconciliation events
- Dashboard endpoints for revenue stats and low-stock alerts
- Swagger/OpenAPI documentation at `/swagger-ui/`
- HikariCP connection pooling with configurable pool size
- PostgreSQL persistence with composite indexes on hot query paths

## Technology Stack

- Framework: Spring Boot 3.4.4
- Language: Java 17
- Database: PostgreSQL
- Cache: Redis
- ORM: Spring Data JPA / Hibernate
- Security: Spring Security + JWT (jjwt 0.11.5) + method-level authorization
- Rate Limiting: Bucket4j 7.6.0
- API Docs: SpringDoc OpenAPI 2.3.0
- Build Tool: Maven (wrapper included)
- Lombok for boilerplate reduction
- H2 in-memory database for tests

## Prerequisites

- JDK 17+
- PostgreSQL 14+
- Redis 6+
- Maven 3.6+ (or use the included `./mvnw` wrapper)

## Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/NotYash1066/shop-management-system.git
cd shop-management-system
```

### 2. Configure Database and Cache

The application reads connection details from environment variables with sensible defaults.

**PostgreSQL** — set these or edit `application.properties`:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/shop_management_system
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password
```

**Redis**:

```
REDIS_URL=redis://localhost:6379
```

**JWT secret** (optional, a default is provided):

```
APP_JWT_SECRET=<your-base64-encoded-secret>
```

### 3. Build

```bash
./mvnw clean install
```

### 4. Run

```bash
./mvnw spring-boot:run
```

Or run the JAR directly:

```bash
java -jar target/shop-management-system-0.0.1-SNAPSHOT.jar
```

The application starts on `http://localhost:8080`.
Swagger UI is available at `http://localhost:8080/swagger-ui/`.

## API Documentation

### Base URL

```
http://localhost:8080/api
```

### Authentication

#### Register a Shop (creates shop + first ADMIN user)

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "admin_user",
  "email": "admin@myshop.com",
  "password": "securepass",
  "shopName": "My Shop"
}
```

#### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin_user",
  "password": "securepass"
}
```

Response includes a JWT token. Pass it as `Authorization: Bearer <token>` on all subsequent requests.

---

### Product Endpoints

All product endpoints require `inventory:read`. Write operations additionally require `inventory:write` (ADMIN only).

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | Get all products for current shop |
| GET | `/api/products/{id}` | Get product by ID (cached) |
| GET | `/api/products/sku/{sku}` | Get product by SKU (cached) |
| GET | `/api/products/category/{categoryId}` | Get products by category |
| POST | `/api/products` | Create product |
| PUT | `/api/products/{id}` | Update product (evicts cache) |
| DELETE | `/api/products/{id}` | Delete product (evicts cache) |

**Create Product**

```http
POST /api/products
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Laptop",
  "price": 999.99,
  "sku": "LAP-001",
  "stockQuantity": 50,
  "lowStockThreshold": 5,
  "category": { "id": 1 },
  "supplier": { "id": 1 }
}
```

---

### Category Endpoints

Require `category:read`. Write operations require `category:write` (ADMIN only).

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/categories` | Get all categories |
| GET | `/api/categories/{id}` | Get category by ID |
| POST | `/api/categories` | Create category |
| PUT | `/api/categories/{id}` | Update category |
| DELETE | `/api/categories/{id}` | Delete category |

**Create Category**

```http
POST /api/categories
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Electronics"
}
```

---

### Supplier Endpoints

Require `supplier:read`. Write operations require `supplier:write` (ADMIN only).

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/suppliers` | Get all suppliers |
| GET | `/api/suppliers/{id}` | Get supplier by ID |
| POST | `/api/suppliers` | Create supplier |
| PUT | `/api/suppliers/{id}` | Update supplier |
| DELETE | `/api/suppliers/{id}` | Delete supplier |

**Create Supplier**

```http
POST /api/suppliers
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Tech Supplies Inc.",
  "contact": "contact@techsupplies.com"
}
```

---

### Employee Endpoints

Require `employee:read`. Write operations require `employee:write` (ADMIN only).

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/employees` | Get all employees |
| GET | `/api/employees/{id}` | Get employee by ID |
| POST | `/api/employees` | Create employee |
| PUT | `/api/employees/{id}` | Update employee |
| DELETE | `/api/employees/{id}` | Delete employee |

**Create Employee**

```http
POST /api/employees
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Jane Smith",
  "role": "Sales Associate",
  "email": "jane@myshop.com"
}
```

---

### Order Endpoints

| Method | Endpoint | Permission | Description |
|--------|----------|------------|-------------|
| POST | `/api/orders` | `order:create` | Place an order |
| GET | `/api/orders/user/{userId}` | `order:read` | Get orders for a user |

Orders are created with `PENDING` status. Stock deduction happens asynchronously. The final status is either `COMPLETED` or `FAILED`. Users can only view their own orders unless they have `dashboard:read` (ADMIN).

**Create Order**

```http
POST /api/orders
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": 1,
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 3, "quantity": 1 }
  ]
}
```

---

### Dashboard Endpoints

Require `dashboard:read` (ADMIN only).

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/dashboard/stats` | Total revenue and order count for the shop |
| GET | `/api/dashboard/low-stock` | Products with stock quantity below 10 |

---

## Role & Permission Matrix

| Permission | ADMIN | USER |
|------------|-------|------|
| `inventory:read` | yes | yes |
| `inventory:write` | yes | no |
| `category:read` | yes | yes |
| `category:write` | yes | no |
| `supplier:read` | yes | no |
| `supplier:write` | yes | no |
| `employee:read` | yes | no |
| `employee:write` | yes | no |
| `order:create` | yes | yes |
| `order:read` | yes | yes |
| `dashboard:read` | yes | no |

---

## Order Reconciliation Flow

1. `POST /api/orders` performs a soft stock check and saves the order as `PENDING`.
2. A `StockReconciliationEvent` is published and picked up by `StockEventListener` on the `stockReconciliationExecutor` thread pool.
3. `StockReconciliationService.reconcileOrder()` acquires a pessimistic lock (`SELECT FOR UPDATE`) on each product row, deducts stock, and marks the order `COMPLETED`.
4. If any step fails, `markOrderFailed()` runs in a new transaction (`REQUIRES_NEW`) and records the failure reason on the order.
5. Both outcomes are written to the audit log asynchronously.

---

## Caching

Product lookups by ID and SKU are cached in Redis under the keys `{shopId}:id:{productId}` and `{shopId}:sku:{sku}`. TTL is 10 minutes. Cache entries are evicted explicitly on product update and delete. `sync=true` on `@Cacheable` prevents cache stampede under concurrent load.

---

## Rate Limiting

Implemented via Bucket4j with per-user / per-IP token buckets:

- Authenticated users: 500 requests/second
- Unauthenticated IPs: 100 requests/second

Responses include `X-Rate-Limit-Remaining` and `Retry-After` headers. Exceeded limits return HTTP 429.

---

## Audit Logging

Every order creation and stock reconciliation event (success or failure) is written to the `audit_logs` table via an `@Async` method, keeping the main request path non-blocking.

---

## Database Schema

Tables managed automatically by Hibernate (`ddl-auto=update`):

| Table | Description |
|-------|-------------|
| `shops` | Multi-tenant root — one row per registered shop |
| `users` | Authentication, linked to a shop, role: ADMIN or USER |
| `products` | Inventory items with SKU, stock quantity, and low-stock threshold |
| `categories` | Product categories, shop-scoped |
| `suppliers` | Supplier records, shop-scoped |
| `employees` | Employee records, shop-scoped |
| `orders` | Orders with status, reconciliation timestamps, and failure reason |
| `order_items` | Line items with price captured at time of sale |
| `audit_logs` | Immutable audit trail for key operations |

Key indexes: composite indexes on `(shop_id, sku)`, `(shop_id, category_id)`, `(shop_id, stockQuantity)`, `(shop_id, status, createdAt)` to support common query patterns.

---

## Configuration Reference

All tuneable values can be set via environment variables:

| Property | Env Variable | Default |
|----------|-------------|---------|
| DB URL | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/shop_management_system` |
| DB Username | `SPRING_DATASOURCE_USERNAME` | `postgres` |
| DB Password | `SPRING_DATASOURCE_PASSWORD` | `password` |
| Redis URL | `REDIS_URL` | `redis://localhost:6379` |
| JWT Secret | `APP_JWT_SECRET` | (built-in default) |
| HikariCP max pool | `DB_POOL_SIZE` | `20` |
| HikariCP min idle | `DB_POOL_MIN_IDLE` | `5` |
| Connection timeout | `DB_CONNECTION_TIMEOUT_MS` | `5000` |
| Async core pool | `ASYNC_STOCK_CORE_POOL_SIZE` | `8` |
| Async max pool | `ASYNC_STOCK_MAX_POOL_SIZE` | `32` |
| Async queue capacity | `ASYNC_STOCK_QUEUE_CAPACITY` | `500` |

---

## Performance Testing

`load_test.py` generates concurrency and latency numbers for your environment:

```bash
python3 load_test.py \
  --url http://localhost:8080/api/products/sku/SKU-1 \
  --method GET \
  --requests 500 \
  --concurrency 50 \
  --headers '{"Authorization":"Bearer <jwt>"}' \
  --payload '{}'
```

See `BENCHMARK_GUIDE.md` for a full walkthrough.

---

## Project Structure

```
shop-management-system/
├── src/
│   ├── main/
│   │   ├── java/com/shopmanagement/
│   │   │   ├── config/       # Security, Cache, Async, RateLimit, OpenAPI
│   │   │   ├── dto/          # Request/response DTOs
│   │   │   ├── entity/       # JPA entities
│   │   │   ├── event/        # StockReconciliationEvent
│   │   │   ├── repository/   # Spring Data JPA repositories
│   │   │   ├── rest/         # REST controllers + GlobalExceptionHandler
│   │   │   ├── security/     # JWT filter, RBAC, UserDetails
│   │   │   └── services/     # Business logic + async reconciliation
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── benchmark.py
├── load_test.py
├── seed.sh
├── nixpacks.toml
├── pom.xml
└── README.md
```

---

## Testing

```bash
./mvnw test
```

Tests use H2 in-memory database. The test suite includes security tests for the product controller and unit tests for `UserDetailsImpl`.

---

## Author

NotYash1066 — [GitHub](https://github.com/NotYash1066)
