# FleetLab

Multi-Robot Fleet Simulation Platform

## Goal

Configure robot fleets, workloads, and control algorithms, run simulations, and compare their performance.

## Stack

Backend:

- Kotlin
- Spring Boot 4.1
- PostgreSQL/PostGIS
- Liquibase

Frontend:

- React
- TypeScript
- Vite

## Prerequisites

- JDK 21
- Node.js 22
- Docker with Docker Compose

## Local Development

Start PostgreSQL/PostGIS:

```bash
docker compose up -d db
```

If port 5432 is already in use, select another local port for both commands:

```bash
FLEETLAB_DB_PORT=5433 docker compose up -d db
```

Run backend tests and start the API:

```bash
cd backend
./gradlew test
./gradlew bootRun
```

When Compose is using a custom port, start the backend with the same value:

```bash
FLEETLAB_DB_PORT=5433 ./gradlew bootRun
```

The local Spring profile is the default for development. Check the application at [Actuator health](http://localhost:8080/actuator/health).

Install frontend dependencies and start Vite:

```bash
cd frontend
npm install
npm run dev
```

Stop local infrastructure:

```bash
docker compose down
```

## Roadmap

- Map graph model
- Robot model
- Path finding
- Simulation loop
- Real-time visualization
