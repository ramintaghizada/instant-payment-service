# Stop the application
sudo fuser -k 8080/tcp 2>/dev/null

# Stop and remove PostgreSQL container
docker-compose down -v

# Remove the volume completely
docker volume rm instant-payment-service_postgres-data 2>/dev/null

# Start fresh
docker-compose up -d postgres

# Wait for PostgreSQL to start
sleep 10

# Run migrations
./mvnw flyway:migrate

# Start the application
./mvnw spring-boot:run &