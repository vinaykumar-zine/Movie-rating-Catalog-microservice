# Movie Catalog Microservices System

A **Spring Boot & Spring Cloud** demonstration system that showcases a microservice architecture for a movie catalog application.  It is split into four independent services that discover each other through Eureka, communicate over REST, and can be containerised with Docker.

> **Services**
>
> | Service                   | Port   | Description                                                                                                             |
> | ------------------------- | ------ | ----------------------------------------------------------------------------------------------------------------------- |
> | **Discovery‑Server**      | `8761` | Eureka service registry so the other services can register/discover each other.                                         |
> | **movie‑info‑service**    | `8081` | Stores and exposes metadata about individual movies (title, description, release year, etc.).                           |
> | **rating‑movie‑service**  | `8083` | Stores and exposes user ratings for each movie.                                                                         |
> | **movie‑catalog‑service** | `8082` | Aggregates data from *movie‑info‑service* and *rating‑movie‑service* to create a personalised catalog for a given user. |

---

## 1. High‑level Architecture

```text
                            ┌────────────────────┐   registers every 30 s   ┌────────────────────┐
                            │  Discovery Server  │◄────────────────────────┤ movie‑info‑service │
                            │ (Eureka, 8761)     │                         └────────────────────┘
                            │                    │
          user call         │                    │                         ┌────────────────────┐
  ───────────────►(8082)────┤                    │◄────────────────────────┤ rating‑movie‑service│
                            │                    │   registers every 30 s  └────────────────────┘
                            │                    │
                            │                    │◄────────────────────────┐
                            └─────────▲──────────┘                         │
                                      │                                    │ registers every 30 s
                                      │                                    │
                             (service discovery)           ┌────────────────────────┐
                                                          │ movie‑catalog‑service │
                                                          └────────────────────────┘
```

- **Discovery Server** is the single source of truth for all running instances.
- Each business micro‑service is completely stateless; scale it horizontally by starting more instances.
- The catalog service calls the other two services by logical service ID, not by hard‑coded host/port.

---

## 2. Service Details

### 2.1 Discovery‑Server

| Item         | Value                       |
| ------------ | --------------------------- |
| Stack        | Spring Cloud Netflix Eureka |
| Default Port | **8761**                    |
| Health URL   | `GET /actuator/health`      |
| Dashboard    | `http://localhost:8761/`    |

#### Run locally

```bash
cd discovery-server
mvn spring-boot:run
```

No custom configuration is needed—`application.yml` already sets `server.port=8761` and disables self‑registration.

---

### 2.2 movie‑info‑service

Exposes basic metadata about movies.

| Item            | Value                                   |
| --------------- | --------------------------------------- |
| Runtime port    | **8081** (overridden via `SERVER_PORT`) |
| Registration ID | `movie-info-service`                    |
| Main endpoint   | `GET /movies/{movieId}`                 |

#### Sample request/response

```bash
curl http://localhost:8081/movies/550
```

```json
{
  "movieId": "550",
  "name": "Fight Club",
  "description": "An insomniac office worker and a soap maker form an underground fight club…",
  "year": 1999
}
```

---

### 2.3 rating‑movie‑service

Provides user ratings for movies.

| Item                                | Value                           |
| ----------------------------------- | ------------------------------- |
| Runtime port                        | **8083**                        |
| Registration ID                     | `rating-movie-service`          |
| Endpoints                           |                                 |
|   `GET /ratingsdata/{movieId}`      | Rating for a single movie       |
|   `GET /ratingsdata/users/{userId}` | All ratings submitted by a user |

#### Sample

```bash
curl http://localhost:8083/ratingsdata/users/user123
```

```json
{
  "userRatings": [
    { "movieId": "550", "rating": 5 },
    { "movieId": "155", "rating": 4 }
  ]
}
```

---

### 2.4 movie‑catalog‑service

Aggregates info + ratings to deliver a ready‑to‑display catalog.

| Item            | Value                   |
| --------------- | ----------------------- |
| Port            | **8082**                |
| Registration ID | `movie-catalog-service` |
| Endpoint        | `GET /catalog/{userId}` |

For each movie the user has rated, the service adds metadata from *movie‑info‑service* and returns a combined JSON:

```bash
curl http://localhost:8082/catalog/user123
```

```json
[
  {
    "name": "Fight Club",
    "description": "An insomniac office worker…",
    "rating": 5
  },
  {
    "name": "The Dark Knight",
    "description": "When the menace known as the Joker wreaks havoc…",
    "rating": 4
  }
]
```

---

## 3. Prerequisites

- **JDK 17** or higher
- **Maven 3.9+**
- **Docker** & **Docker Compose** (optional but recommended)

---

## 4. Running Everything Locally (dev workflow)

```bash
# 0) clone the repo and cd into it

# 1) start discovery first
cd discovery-server && mvn spring-boot:run &

# 2) in new terminals, start each service
cd ../movie-info-service    && mvn spring-boot:run &
cd ../rating-movie-service  && mvn spring-boot:run &
cd ../movie-catalog-service && mvn spring-boot:run &

# 3) open Eureka dashboard
xdg-open http://localhost:8761 &

# 4) test
curl http://localhost:8082/catalog/user123
```

Stop everything with `kill %1 %2 %3 %4` (or close the terminals).

---

## 5. Docker & Docker Compose

The project ships with a `docker-compose.yml` that spins up one container per service plus a network.  Build images and start:

```bash
# Build images
mvn clean package -DskipTests

# Start the whole stack
docker compose up --build
```

Within 30 seconds the services will register with Eureka.  Access as usual:

- Eureka dashboard: `http://localhost:8761/`
- Movie catalog: `http://localhost:8082/catalog/user123`

Scale any service horizontally:

```bash
docker compose up --scale movie-info-service=3 --scale rating-movie-service=2 -d
```

---

## 6. Environment Variables & Configuration

| Variable                               | Default                               | Consumed by  | Purpose                                      |
| -------------------------------------- | ------------------------------------- | ------------ | -------------------------------------------- |
| `SERVER_PORT`                          | service‑specific                      | all services | Override HTTP port                           |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | `http://discovery-server:8761/eureka` | all services | Eureka URL when running under Docker Compose |
| `SPRING_PROFILES_ACTIVE`               | `default`                             | all          | Activate production or dev profile           |

---

## 7. Building

```bash
mvn clean verify
```

Unit & integration tests run automatically; resulting jar is placed into `target/`.

---

## 8. CI/CD Tips

Because every service is independent, prefer one pipeline per service.

1. **Build & Test** ➜ produce Docker image with semver tag.
2. **Push** ➜ Registry (GitHub Container Registry, ECR, GCR...).
3. **Deploy** ➜ K8s `Deployment` referencing the new image tag.
4. **Smoke Test** ➜ Call `/actuator/health`.

---

## 9. Useful Links

- Spring Cloud Netflix: [https://spring.io/projects/spring-cloud-netflix](https://spring.io/projects/spring-cloud-netflix)
- Eureka Docs: [https://github.com/Netflix/eureka](https://github.com/Netflix/eureka)
- Docker Compose: [https://docs.docker.com/compose/](https://docs.docker.com/compose/)

---

## 10. Contact

Maintainer: **Your Name **[**your@email.com**](mailto\:your@email.com)\
Issues & PRs are welcome!

