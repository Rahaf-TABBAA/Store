# Shop Application

A comprehensive Spring Boot e-commerce application with Keycloak authentication, PostgreSQL database, and modern layered architecture.

## Features

### Backend Architecture
- **Spring Boot 3.2** with Java 17
- **Layered Architecture**: Controller → Service → Repository
- **JPA/Hibernate** for data persistence
- **PostgreSQL** database with Flyway migrations
- **Keycloak** integration for authentication and authorization
- **JWT-based security** for API access
- **RESTful APIs** with comprehensive CRUD operations
- **Exception handling** with global error handling
- **Logging** with SLF4J and Logback
- **Unit and Integration tests** with JUnit 5 and Mockito

### Domain Model
- **Product Management**: Products with categories, stock management
- **User Management**: User profiles with roles (ADMIN, CUSTOMER)
- **Order Management**: Orders with order items, status tracking
- **Category Management**: Product categorization

### Security & Authorization
- **Keycloak Integration**: OAuth2/OpenID Connect
- **Role-based Access Control**: Admin and Customer roles
- **JWT Token Validation**: Secure API endpoints
- **CORS Configuration**: Cross-origin request support

### Additional Features
- **Database Migrations**: Flyway for schema versioning
- **Docker Support**: Full containerization with Docker Compose
- **API Documentation**: Ready for Swagger/OpenAPI integration
- **Health Checks**: Spring Actuator endpoints
- **Production Ready**: Comprehensive configuration

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker and Docker Compose
- PostgreSQL 12+ (if running locally)

### Running with Docker (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Store
   ```

2. **Start the development environment**
   ```bash
   docker-compose -f docker-compose.dev.yml up -d
   ```

   This will start:
   - PostgreSQL database (port 5432)
   - Keycloak authentication server (port 8180)
   - pgAdmin for database management (port 5050)

3. **Build and run the application**
   ```bash
   mvn clean package -DskipTests
   docker-compose up --build
   ```

### Running Locally

1. **Start PostgreSQL and Keycloak**
   ```bash
   docker-compose -f docker-compose.dev.yml up postgres-dev keycloak-dev -d
   ```

2. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

## API Endpoints

### Public Endpoints
- `GET /api/products` - List all products (with pagination)
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/search?keyword={keyword}` - Search products
- `GET /api/categories` - List all categories
- `GET /api/categories/{id}` - Get category by ID

### Customer Endpoints (Requires Authentication)
- `GET /api/users/me` - Get current user profile
- `PUT /api/users/{id}` - Update user profile (own profile)
- `GET /api/orders/my-orders` - Get current user's orders
- `POST /api/orders` - Create new order
- `POST /api/orders/{id}/items` - Add items to order
- `PATCH /api/orders/{id}/cancel` - Cancel order

### Admin Endpoints (Requires ADMIN Role)
- `POST /api/categories` - Create category
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category
- `POST /api/products` - Create product
- `PUT /api/products/{id}` - Update product
- `PATCH /api/products/{id}/stock` - Update product stock
- `DELETE /api/products/{id}` - Delete product
- `GET /api/users` - List all users
- `POST /api/users` - Create user
- `GET /api/orders` - List all orders
- `PATCH /api/orders/{id}/status` - Update order status

## Database Schema

### Tables
- **categories**: Product categories
- **users**: User accounts with Keycloak integration
- **products**: Product catalog with stock management
- **orders**: Customer orders
- **order_items**: Individual items within orders

### Key Relationships
- Products belong to Categories (Many-to-One)
- Orders belong to Users (Many-to-One)
- OrderItems link Orders and Products (Many-to-Many through junction table)

## Keycloak Configuration

### Realm Setup
1. Access Keycloak Admin Console: http://localhost:8180
2. Login with admin/admin
3. Create realm: `shop-realm`
4. Create client: `shop-client`
5. Create roles: `ADMIN`, `CUSTOMER`
6. Create test users and assign roles

### Client Configuration
- Client ID: `shop-client`
- Client Protocol: `openid-connect`
- Access Type: `public`
- Valid Redirect URIs: `http://localhost:3000/*` (for frontend)

## Testing

### Running Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CategoryServiceTest

# Run with coverage
mvn test jacoco:report
```

### Test Categories
- **Unit Tests**: Service layer testing with Mockito
- **Integration Tests**: Full application context testing
- **Repository Tests**: JPA repository testing

## Configuration

### Environment Variables
- `SPRING_PROFILES_ACTIVE`: Active Spring profile (default, docker)
- `POSTGRES_DB`: PostgreSQL database name
- `POSTGRES_USER`: PostgreSQL username
- `POSTGRES_PASSWORD`: PostgreSQL password
- `KEYCLOAK_ADMIN`: Keycloak admin username
- `KEYCLOAK_ADMIN_PASSWORD`: Keycloak admin password
- `JWT_SECRET`: JWT signing secret
- `CORS_ORIGINS`: Allowed CORS origins

### Application Profiles
- **default**: Development profile with local database
- **docker**: Docker environment profile
- **test**: Testing profile with H2 database

## Development

### Project Structure
```
src/main/java/com/shop/
├── controller/          # REST controllers
├── service/            # Business logic services
├── repository/         # Data access repositories
├── entity/            # JPA entities
├── dto/               # Data Transfer Objects
├── mapper/            # MapStruct mappers
├── config/            # Configuration classes
└── exception/         # Exception handling

src/main/resources/
├── db/migration/      # Flyway database migrations
├── application.yml    # Main configuration
├── application-docker.yml
└── application-test.yml

src/test/
├── java/com/shop/     # Test classes
└── resources/         # Test resources
```

### Adding New Features
1. Create entity classes in `entity` package
2. Create repository interfaces in `repository` package
3. Create DTOs in `dto` package
4. Create MapStruct mappers in `mapper` package
5. Implement business logic in `service` package
6. Create REST controllers in `controller` package
7. Write tests for all layers
8. Create database migrations if needed

## Production Deployment

### Docker Production Build
```bash
# Build production image
docker build -t shop-app:latest .

# Run production stack
docker-compose -f docker-compose.yml up -d
```

### Security Considerations
- Change default passwords
- Use environment variables for secrets
- Configure HTTPS/TLS
- Set up proper CORS origins
- Configure Keycloak realm security settings
- Use production-grade database
- Set up monitoring and logging

## Monitoring

### Health Checks
- Application: http://localhost:8080/actuator/health
- Keycloak: http://localhost:8180/health
- Database: Check via pgAdmin or direct connection

### Metrics
- Application metrics: http://localhost:8080/actuator/metrics
- Custom business metrics can be added using Micrometer

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For questions and support, please create an issue in the repository or contact the development team.