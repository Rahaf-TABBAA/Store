#!/bin/bash

# Development startup script for Shop Application

echo "Starting Shop Application Development Environment..."

# Function to check if a service is running
check_service() {
    local service_name=$1
    local url=$2
    local max_attempts=30
    local attempt=1
    
    echo "Checking $service_name..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f -s "$url" > /dev/null 2>&1; then
            echo "$service_name is ready!"
            return 0
        fi
        echo "Attempt $attempt/$max_attempts: $service_name not ready yet..."
        sleep 5
        ((attempt++))
    done
    
    echo "$service_name failed to start after $max_attempts attempts"
    return 1
}

# Start infrastructure services
echo "Starting PostgreSQL and Keycloak..."
docker-compose -f docker-compose.dev.yml up -d postgres-dev keycloak-dev pgadmin

# Wait for services to be ready
check_service "PostgreSQL" "http://localhost:5432" || exit 1
check_service "Keycloak" "http://localhost:8180/health/ready" || exit 1

echo "Infrastructure services are ready!"
echo "You can now:"
echo "  - Access Keycloak Admin Console: http://localhost:8180 (admin/admin)"
echo "  - Access pgAdmin: http://localhost:5050 (admin@shop.com/admin)"
echo "  - Start the Spring Boot application: mvn spring-boot:run"
echo "  - Or run the full stack: docker-compose up --build shop-app"

echo "\nUseful commands:"
echo "  - View logs: docker-compose -f docker-compose.dev.yml logs -f [service]"
echo "  - Stop services: docker-compose -f docker-compose.dev.yml down"
echo "  - Reset database: docker-compose -f docker-compose.dev.yml down -v"