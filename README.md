# BookMyShow Backend - Production Grade

A production-grade BookMyShow-like backend application built with Java 21, Spring Boot 3.x, PostgreSQL, Redis, and Kafka.

## Architecture

This application follows a modular monolith architecture with clear service boundaries that can be extracted into microservices when needed:

```
├── user-service          # Authentication, authorization, user management
├── catalog-service       # Movies, cities, venues, screens, seats
├── show-service          # Show scheduling, pricing
├── booking-service       # Seat holds, booking lifecycle, concurrency
├── payment-service       # Payment processing, webhooks, refunds
├── notification-service  # Email/SMS/push notifications
└── common                # Shared infrastructure (security, exceptions, Kafka, etc.)
```

## Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.2.x
- **Database**: PostgreSQL 16 with Flyway migrations
- **Cache**: Redis 7
- **Messaging**: Apache Kafka
- **Security**: Spring Security, JWT, OAuth2/OIDC, BCrypt
- **Observability**: Micrometer, Prometheus, Grafana, OpenTelemetry
- **Resilience**: Resilience4j (Circuit Breaker, Retry, Rate Limiter)
- **Testing**: JUnit 5, Mockito, Testcontainers, AssertJ
- **Build**: Maven
- **Container**: Docker multi-stage builds
- **Orchestration**: Kubernetes

## Key Features

### 1. Seat Booking Concurrency
- Pessimistic locking with `SELECT ... FOR UPDATE`
- Seat hold mechanism with TTL (10 minutes default)
- Database-level unique constraint on `(show_id, seat_id)`
- Idempotency keys for all write operations

### 2. Payment Processing
- Idempotent payment initiation
- Webhook signature verification
- Payment state machine (INITIATED → PENDING → SUCCESS/FAILED → REFUNDED)
- Refund support with idempotency

### 3. Transactional Outbox Pattern
- Guarantees event publishing after DB commit
- Automatic retry with exponential backoff
- Dead letter queue handling

### 4. Caching Strategy
- Cache-aside pattern with Redis
- TTL with jitter to prevent stampedes
- Separate cache configs for catalog, shows, seat availability

### 5. Rate Limiting & Resilience
- Per-endpoint rate limits (auth: 10/min, API: 100/min, booking: 30/min)
- Circuit breakers for external dependencies
- Configurable timeouts and retries

## Getting Started

### Prerequisites
- Java 21
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 16 (or use Docker)
- Redis 7 (or use Docker)
- Kafka (or use Docker)

### Local Development with Docker Compose

```bash
# Start all dependencies
docker-compose up -d postgres redis kafka zookeeper keycloak

# Run the application
./mvnw spring-boot:run
```

### Running Tests

```bash
# Unit tests
./mvnw test

# Integration tests (requires Testcontainers)
./mvnw verify -DskipUnitTests=false

# With coverage
./mvnw verify jacoco:report
```

### Building Docker Image

```bash
./mvnw spring-boot:build-image -Pdocker
```

## API Endpoints

### Authentication
```
POST   /api/v1/auth/register     # Register new user
POST   /api/v1/auth/login        # Login
POST   /api/v1/auth/refresh      # Refresh access token
GET    /api/v1/users/me          # Get current user profile
PATCH  /api/v1/users/me          # Update profile
POST   /api/v1/users/me/change-password
```

### Catalog
```
GET    /api/v1/cities                    # List all cities
GET    /api/v1/cities/{id}               # Get city details
GET    /api/v1/venues?cityId={id}        # List venues in city
GET    /api/v1/screens?venueId={id}      # List screens in venue
GET    /api/v1/screens/{id}/layout       # Get screen seat layout
GET    /api/v1/movies/now-playing        # Movies currently playing
GET    /api/v1/movies/coming-soon        # Upcoming movies
GET    /api/v1/movies                    # Search movies
GET    /api/v1/movies/{id}               # Movie details
GET    /api/v1/movies/genres             # All genres
```

### Shows
```
GET    /api/v1/shows/movie/{movieId}           # Shows for a movie
GET    /api/v1/shows/city/{cityId}             # Shows in city (date range)
GET    /api/v1/shows/{id}                      # Show details
GET    /api/v1/shows/{id}/availability         # Seat availability
```

### Booking
```
POST   /api/v1/bookings/hold       # Hold seats (10 min)
POST   /api/v1/bookings/{id}/confirm  # Confirm booking after payment
GET    /api/v1/bookings/{id}       # Get booking details
GET    /api/v1/bookings            # List user bookings
POST   /api/v1/bookings/{id}/cancel # Cancel booking
```

### Payment
```
POST   /api/v1/payments            # Initiate payment
GET    /api/v1/payments/{id}       # Payment details
POST   /api/v1/payments/refund     # Request refund
POST   /api/v1/payments/webhook    # Payment provider webhook
```

## Configuration

Key configuration properties (set via environment variables):

```yaml
# Database
DATABASE_URL: jdbc:postgresql://localhost:5432/bookmyshow
DATABASE_USERNAME: postgres
DATABASE_PASSWORD: postgres

# Redis
REDIS_HOST: localhost
REDIS_PORT: 6379

# Kafka
KAFKA_BOOTSTRAP_SERVERS: localhost:9092
KAFKA_CONSUMER_GROUP: bookmyshow

# Security
JWT_SECRET: your-256-bit-secret-key
KEYCLOAK_ISSUER_URI: http://localhost:8080/realms/bookmyshow
KEYCLOAK_CLIENT_ID: bookmyshow-client
KEYCLOAK_CLIENT_SECRET: secret

# Booking
BOOKING_SEAT_HOLD_DURATION_MINUTES: 10
BOOKING_MAX_SEATS_PER_BOOKING: 10

# Payment
RAZORPAY_KEY_ID: your-key
RAZORPAY_KEY_SECRET: your-secret
RAZORPAY_WEBHOOK_SECRET: your-webhook-secret
```

## Database Migrations

Flyway migrations are in `src/main/resources/db/migration/`:

```
V1__create_user.sql
V2__create_catalog.sql
V3__create_movie.sql
V4__create_show.sql
V5__create_booking.sql
V6__create_payment_notification.sql
```

Run migrations:
```bash
./mvnw flyway:migrate
```

## Observability

### Health Checks
- `GET /actuator/health/liveness` - Liveness probe
- `GET /actuator/health/readiness` - Readiness probe
- `GET /actuator/health/startup` - Startup probe

### Metrics
- `GET /actuator/prometheus` - Prometheus metrics
- HTTP latency (p50, p95, p99)
- JVM/GC metrics
- Database pool saturation
- Kafka consumer lag
- Business metrics (booking success rate, payment success rate)

### Logging
Structured JSON logging with correlation IDs:
```json
{
  "timestamp": "2024-01-15T10:30:00.123Z",
  "level": "INFO",
  "service": "booking-service",
  "traceId": "abc-123",
  "requestId": "req-456",
  "bookingId": "booking-789",
  "event": "BOOKING_CONFIRMED"
}
```

## Deployment

### Kubernetes

```bash
# Apply manifests
kubectl apply -f k8s/base/

# Or use kustomize for environments
kubectl apply -k k8s/overlays/prod
```

### CI/CD Pipeline

GitHub Actions workflow (`.github/workflows/ci-cd.yml`):
1. Build & Unit Tests
2. Integration Tests (Testcontainers)
3. Static Analysis (SpotBugs, PMD, Checkstyle)
4. Security Scan (Trivy, OWASP Dependency Check)
5. Docker Build & Push
6. Deploy to Dev → Staging → Production

## Testing Strategy

- **Unit Tests**: Business logic, validation, state machines
- **Integration Tests**: Repository layer, database behavior (Testcontainers)
- **Contract Tests**: API contracts, event schemas
- **E2E Tests**: Critical user journeys
- **Performance Tests**: Load testing with k6/Gatling
- **Chaos Tests**: Failure injection (Litmus/Chaos Mesh)

## Important Design Decisions

1. **PostgreSQL as Source of Truth**: Seat inventory and booking state managed in PostgreSQL with pessimistic locking
2. **Redis for Acceleration Only**: Caching and rate limiting, never for seat ownership
3. **Kafka for Decoupling**: Async event propagation, not for transactional state
4. **Modular Monolith First**: Clear service boundaries, extract when scaling/ownership demands
5. **Idempotency Everywhere**: All retryable operations have idempotency keys
6. **Outbox Pattern**: Guarantees DB + Kafka atomicity

## License

MIT License