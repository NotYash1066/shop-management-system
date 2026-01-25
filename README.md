# Shop Management System 🏪

A comprehensive RESTful API-based shop management system built with Spring Boot, designed to handle all aspects of retail shop operations including product management, employee tracking, supplier management, and category organization.

## 📋 Overview

The Shop Management System is a robust backend solution that provides a complete set of APIs for managing a retail shop's operations. It offers secure authentication, CRUD operations for all entities, and maintains relational integrity between products, categories, and suppliers.

## ✨ Key Features

- **Product Management**: Complete CRUD operations for products with category and supplier associations
- **Category Management**: Organize products into hierarchical categories
- **Supplier Management**: Track and manage supplier information
- **Employee Management**: Maintain employee records and information
- **User Authentication**: Secure user registration and login functionality
- **RESTful API**: Clean and intuitive REST endpoints for all operations
- **Spring Security**: Built-in security features for protected endpoints
- **MySQL Database**: Persistent data storage with JPA/Hibernate
- **Automatic Schema Management**: Database tables auto-created and updated

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.4.4
- **Language**: Java 17
- **Database**: MySQL 8.x
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Spring Security
- **Build Tool**: Maven
- **Additional Libraries**:
  - Lombok (for reducing boilerplate code)
  - Jackson (for JSON processing)

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

### 2. Configure Database

The application is configured to use MySQL. You have two options:

**Option A: Use default configuration (recommended for quick start)**
- Create a MySQL database (it will be auto-created if it doesn't exist)
- The default configuration uses:
  - Database: `shop_management_system`
  - Username: `root`
  - Password: (empty)
  - Port: `3306`

**Option B: Custom database configuration**

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/your_database_name?createDatabaseIfNotExist=true
spring.datasource.username=your_username
spring.datasource.password=your_password
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

## 🗄️ Database Schema

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
spring.datasource.url=jdbc:mysql://localhost:3306/shop_management_system?createDatabaseIfNotExist=true
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

## 🧪 Testing

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

## 🐛 Known Issues

- Authentication endpoints are currently placeholder implementations
- Password encryption should be added before production use
- Consider adding proper JWT token-based authentication

## 🚀 Future Enhancements

- Add JWT authentication
- Implement role-based access control
- Add inventory tracking
- Sales and transaction management
- Reporting and analytics
- API documentation with Swagger/OpenAPI
- Docker containerization
- Unit and integration tests

## 📞 Support

If you encounter any issues or have questions, please open an issue on GitHub.

---

**Happy Coding! 🎉**

