# Shop Management System

A multi-tenant REST API for retail operations built with Spring Boot. The current codebase focuses on secure shop-scoped inventory management, order processing, asynchronous stock reconciliation, and Redis-backed caching for hot inventory lookups.

## 📋 Overview

The Shop Management System is a robust backend solution that provides a complete set of APIs for managing a retail shop's operations. It offers secure authentication, CRUD operations for all entities, and maintains relational integrity between products, categories, and suppliers.

## Key Features

- Product inventory CRUD with shop-level SKU uniqueness
- Redis-backed caching for inventory lookups by product ID and SKU
- Asynchronous stock reconciliation using Spring Events and a dedicated executor
- JWT authentication with permission-based RBAC enforced through Spring Security
- Shop-scoped category, supplier, employee, order, and dashboard APIs
- PostgreSQL persistence with JPA/Hibernate and query-oriented indexes
- Load-testing helper script for concurrency and latency checks

## Technology Stack

- **Framework**: Spring Boot 3.4.4
- **Language**: Java 17
- **Database**: PostgreSQL
- **Cache**: Redis
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Spring Security + JWT + method-level authorization
- **Build Tool**: Maven
- **Additional Libraries**:
  - Lombok (for reducing boilerplate code)
  - Spring Cache
  - Bucket4j
  - SpringDoc OpenAPI

## 📋 Prerequisites

Before setting up the project, ensure you have the following installed:

- **Java Development Kit (JDK) 17** or higher
- **Maven 3.6+** (or use the included Maven wrapper)
- **MySQL Server 8.0+**
- **Git** (for cloning the repository)

## 🚀 Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/NotYash1066/shop-management-system.git
cd shop-management-system
```

### 2. Configure Database And Cache

The application is configured to use PostgreSQL and Redis by default.

**PostgreSQL**

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/your_database_name
spring.datasource.username=your_username
spring.datasource.password=your_password
```

**Redis**

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

### 3. Build the Project

Using Maven wrapper (recommended):
```bash
./mvnw clean install
```

Or using system Maven:
```bash
mvn clean install
```

### 4. Run the Application

Using Maven wrapper:
```bash
./mvnw spring-boot:run
```

Or using system Maven:
```bash
mvn spring-boot:run
```

Or run the generated JAR:
```bash
java -jar target/shop-management-system-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080` by default.

## 📖 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints

#### Register a New User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "password": "secure_password",
  "role": "USER"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "secure_password"
}
```

### Product Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | Get all products |
| GET | `/api/products/{id}` | Get product by ID |
| GET | `/api/products/category/{categoryId}` | Get products by category |
| POST | `/api/products` | Create new product |
| PUT | `/api/products/{id}` | Update product |
| DELETE | `/api/products/{id}` | Delete product |

**Example: Create Product**
```http
POST /api/products
Content-Type: application/json

{
  "name": "Laptop",
  "price": 999.99,
  "category": {
    "id": 1
  },
  "supplier": {
    "id": 1
  }
}
```

### Category Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/categories` | Get all categories |
| GET | `/api/categories/{id}` | Get category by ID |
| POST | `/api/categories` | Create new category |
| PUT | `/api/categories/{id}` | Update category |
| DELETE | `/api/categories/{id}` | Delete category |

**Example: Create Category**
```http
POST /api/categories
Content-Type: application/json

{
  "name": "Electronics",
  "description": "Electronic items and gadgets"
}
```

### Supplier Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/suppliers` | Get all suppliers |
| GET | `/api/suppliers/{id}` | Get supplier by ID |
| POST | `/api/suppliers` | Create new supplier |
| PUT | `/api/suppliers/{id}` | Update supplier |
| DELETE | `/api/suppliers/{id}` | Delete supplier |

**Example: Create Supplier**
```http
POST /api/suppliers
Content-Type: application/json

{
  "name": "Tech Supplies Inc.",
  "contactInfo": "contact@techsupplies.com",
  "phone": "+1-555-0123"
}
```

### Employee Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/employees` | Get all employees |
| GET | `/api/employees/{id}` | Get employee by ID |
| POST | `/api/employees` | Create new employee |
| PUT | `/api/employees/{id}` | Update employee |
| DELETE | `/api/employees/{id}` | Delete employee |

**Example: Create Employee**
```http
POST /api/employees
Content-Type: application/json

{
  "name": "Jane Smith",
  "position": "Sales Associate",
  "email": "jane.smith@shop.com",
  "phone": "+1-555-0456"
}
```

## 💡 Usage Examples

### Using cURL

**1. Register a new user:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","role":"ADMIN"}'
```

**2. Create a category:**
```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Electronics","description":"Electronic items"}'
```

**3. Get all products:**
```bash
curl http://localhost:8080/api/products
```

### Using Postman

1. Import the base URL: `http://localhost:8080/api`
2. Create a new request for each endpoint
3. Set the appropriate HTTP method (GET, POST, PUT, DELETE)
4. Add request body in JSON format for POST/PUT requests
5. Send the request and view the response

## Performance Notes

- Inventory reads use DTO projection queries to avoid unnecessary entity hydration on hot paths.
- Product lookups by ID and SKU are cached in Redis with cache stampede protection via `@Cacheable(sync = true)`.
- Stock updates triggered by order placement run asynchronously on a dedicated reconciliation executor so the order API can respond without waiting for inventory writes.
- Product and order tables include shop-aware indexes to support the common lookup and dashboard patterns.
- `load_test.py` can be used to generate concurrency and latency numbers for your environment.

Example:

```bash
python3 load_test.py --url http://localhost:8080/api/products/sku/SKU-1 --method GET --requests 500 --concurrency 50 --headers '{"Authorization":"Bearer <jwt>"}' --payload '{}'
```

## Database Schema

The application automatically creates the following tables:

- **users**: User authentication and authorization
- **products**: Product information with foreign keys to categories and suppliers
- **categories**: Product categories
- **suppliers**: Supplier information
- **employees**: Employee records

The schema is automatically managed by Hibernate with the `ddl-auto=update` configuration.

## 🔧 Configuration

### Application Properties

Key configuration options in `application.properties`:

```properties
# Application name
spring.application.name=shop-management-system

# Database configuration
spring.datasource.url=jdbc:mysql://localhost:3306/shop_management_system?createDatabaseIfNotExists=true
spring.datasource.username=root
spring.datasource.password=

# JPA/Hibernate configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

### Customization Options

- **Change Port**: Add `server.port=8081` to run on a different port
- **Database Pool**: Configure connection pooling for production
- **Logging**: Adjust logging levels in application.properties
- **Security**: Customize Spring Security settings in SecurityConfig.java

## Testing

Run the test suite:

```bash
./mvnw test
```

Or with system Maven:
```bash
mvn test
```

## 📦 Project Structure

```
shop-management-system/
├── src/
│   ├── main/
│   │   ├── java/com/shopmanagement/
│   │   │   ├── config/          # Configuration classes (Security, etc.)
│   │   │   ├── entity/          # JPA entities (Product, Category, etc.)
│   │   │   ├── repository/      # Spring Data JPA repositories
│   │   │   ├── rest/            # REST controllers
│   │   │   └── ShopManagementSystemApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/                    # Test files
├── pom.xml                      # Maven configuration
└── README.md
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📝 License

This project is open source and available under the [MIT License](LICENSE).

## 👤 Author

**NotYash1066**

- GitHub: [@NotYash1066](https://github.com/NotYash1066)

## Current Gaps

- The repository now supports the architecture behind the concurrency and RBAC claims, but benchmark numbers such as "40% lower DB overhead" or "sub-50ms latency" still need to be measured in a running environment.
- There is not yet a repeatable benchmark report checked into the repo.
- Some controllers still expose entities directly instead of dedicated response DTOs.

## 📞 Support

If you encounter any issues or have questions, please open an issue on GitHub.

---

**Happy Coding! 🎉**
