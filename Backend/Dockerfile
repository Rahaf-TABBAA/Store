FROM openjdk:17-jre-slim

# Set working directory
WORKDIR /app

# Copy the application JAR
COPY target/*.jar app.jar

# Create a non-root user
RUN groupadd -r appuser && useradd --no-log-init -r -g appuser appuser
USER appuser

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]