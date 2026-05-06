# O2 Medical Internal Operations App

Small internal web application for O2 Medical to manage rentals, clients, inventory, invoices, alerts, and daily operations in a digital way.

This project is intentionally narrow in scope. It is built for only two internal users:

- Hamouda, the Agent
- Ahmed Berrada, the Doctor

The goal is to replace repetitive manual tracking with a simple, premium, easy-to-use workspace that helps these two people run the day-to-day activity faster and with fewer errors.

## What the app does

The application covers the main operational flow of a small medical equipment rental business:

- manage devices and consumables
- create and follow rental contracts
- deploy devices and schedule pickups
- record payments and track invoices
- surface urgent alerts, overdue items, and maintenance needs
- keep a clean overview of the business on a dashboard

## Tech Stack

- Frontend: React 18, Vite, TypeScript, Tailwind CSS, Zustand, React Router
- Backend: Spring Boot 3.2, Spring Security, Spring Data JPA, Hibernate 6, Maven
- Database: PostgreSQL
- API style: REST

## Repository Layout

```text
backend/
  src/main/java/com/o2medical/
    api/controller/   REST controllers
    config/           Spring and security configuration
    domain/           Entities and enums
    dto/              API DTOs
    repository/       JPA repositories
    service/          Business logic
  src/main/resources/
    application.properties
  db/migrations/      SQL migration scripts
  pom.xml

frontend/
  src/
    components/       Pages and shared UI
    store/            Global app state
    styles/           Global styling
    App.tsx           Router and shell
    main.tsx          Frontend entry point
  package.json
  vite.config.ts

DATABASE_SCHEMA.sql   Main schema bootstrap
README.md             Project overview and run instructions
```

## Local Setup

### Prerequisites

- Java 17 or newer
- Maven 3.8 or newer
- Node.js 18 or newer
- PostgreSQL 14 or newer

### 1. Create the database

```bash
createdb o2_medical_db
```

If the database already exists, make sure it is empty or aligned with the schema and migration scripts in the repository.

### 2. Apply the schema

```bash
psql o2_medical_db < DATABASE_SCHEMA.sql
```

If you are starting from an older schema, review the SQL files in `backend/db/migrations/` first.

### 3. Configure the backend

Edit `backend/src/main/resources/application.properties` if needed:

```properties
server.port=8080
server.servlet.context-path=/api
spring.datasource.url=jdbc:postgresql://localhost:5432/o2_medical_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

Important notes:

- The backend runs on port `8080`
- The API base path is `/api`
- The frontend is configured to proxy `/api` to the backend during development

### 4. Start the backend

```bash
cd backend
mvn spring-boot:run
```

The backend should be available at:

- Application: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/api/swagger-ui.html`

### 5. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend should be available at `http://localhost:3000`.

## Login Credentials

The app seeds two internal users in the database:

- Agent: `hamouda`
- Doctor: `ahmed`

Authentication is database-backed and password hashes are stored with BCrypt. There is no public registration flow. The initial passwords are defined in the backend seed logic and should be changed before any real deployment.

## Main User Flows

### Agent

- open the dashboard and review daily KPIs
- create rental contracts
- deploy devices
- schedule pickups
- complete rentals
- record payments
- monitor overdue items, maintenance needs, and low-stock consumables

### Doctor

- review operational information relevant to the business
- inspect clients and device-related data
- follow the same internal workspace without needing admin-level changes

## Developer Commands

### Backend

```bash
cd backend
mvn clean install -DskipTests
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
npm run build
npm run lint
npm run type-check
```

## Key Backend Endpoints

The most important API groups are:

- `GET /api/dashboard/agent`
- `POST /api/auth/login`
- `GET /api/devices/...`
- `GET /api/clients/...`
- `GET /api/rental-contracts/...`
- `GET /api/invoices/...`
- `GET /api/alerts/...`

The frontend uses the dashboard and workflow endpoints through the Vite proxy during development.

## Best Practices Used in This Repo

- clear separation between frontend, backend, and database migrations
- REST endpoints grouped by business domain
- database-backed authentication rather than hardcoded UI-only login
- seeded internal users to support first-run setup
- reusable UI components in the frontend
- centralized error handling on the backend
- generated build artifacts kept out of source control through `.gitignore`

## Troubleshooting

### Dashboard shows offline

- confirm the backend is running on port `8080`
- confirm the frontend is running on port `3000`
- confirm `/api/dashboard/agent` returns JSON in the browser or with `curl`
- restart the backend if you changed `application.properties`

### Login fails

- confirm the backend database is reachable
- use one of the seeded users listed above
- verify that the `users` table contains the expected records

### API requests return 404

- make sure the backend context path is `/api`
- make sure the frontend is using the Vite proxy configuration in `frontend/vite.config.ts`

## Notes for Future Development

- keep the app small and focused on the two internal users
- add new screens only when they directly support the operational workflow
- keep migrations in `backend/db/migrations/`
- run `npm run build` and `mvn clean install -DskipTests` before delivery

## Status

Current state: working internal application for a very small team, ready for local development and client demonstration.
