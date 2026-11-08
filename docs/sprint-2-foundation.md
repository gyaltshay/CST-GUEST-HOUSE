# Sprint 2 Foundation

This revision combines the team's backend, inventory, guest routing and CI contributions. The public availability page checks service health. Booking, sign-in and admin screens arrive in later sprints.

## Local run

Use Java 21, the Node version in frontend/.nvmrc, and Docker Compose. Copy .env.example to .env, set a local database password and run docker compose up -d --wait postgres.

In PowerShell, set DATABASE_URL to jdbc:postgresql://127.0.0.1:55432/guesthouse and set POSTGRES_USER and POSTGRES_PASSWORD to match .env. Run backend/mvnw.cmd spring-boot:run from the backend directory (use .\mvnw.cmd there). Check http://127.0.0.1:8080/actuator/health.

In a second terminal run npm --prefix frontend ci and npm --prefix frontend run dev from the repo root. Open http://127.0.0.1:5173/ and follow the Room availability link.

## Verification

Run npm --prefix frontend test and npm --prefix frontend run build. GitHub Actions builds the backend, starts it on PostgreSQL, checks service health and facility seed data, and runs the frontend build and smoke tests. Later booking and management tests should be introduced with their features.
