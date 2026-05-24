# Football Results App

Java 17 / Spring Boot and React sample app for football match results and standings.

## Structure

- `backend`: Spring Boot REST API with H2, JPA, and transaction examples
- `frontend`: React + Vite UI for local preview
- `docs/architecture.md`: 現状の簡単な設計メモ / simple architecture notes
- `docs/setup-commands.md`: 一から実装した場合のコマンドメモ / setup commands from scratch

## Local Run

Backend:

```powershell
cd backend
mvn spring-boot:run
```

Frontend:

```powershell
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

The backend runs on `http://localhost:8081` by default.

## Docker Desktop Run

Build and start both containers from the repository root:

```powershell
docker compose up --build
```

Open `http://localhost:5173`.

The compose setup runs:

- `frontend`: React static files served by nginx on host port `5173`
- `backend`: Spring Boot API on host port `8081`

The frontend calls `/api`, and nginx proxies those requests to the backend container. The backend also exposes actuator health endpoints for future Kubernetes readiness and liveness probes:

- `http://localhost:8081/actuator/health/readiness`
- `http://localhost:8081/actuator/health/liveness`

Stop the containers with:

```powershell
docker compose down
```

### Future Deployment Notes

The backend datasource and CORS settings are controlled by environment variables so the same image can be moved toward Kubernetes and a managed database later:

- `APP_CORS_ALLOWED_ORIGIN`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_DRIVER_CLASS_NAME`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`

MySQL is not enabled yet. When introducing it, add the MySQL JDBC driver to the backend and set the datasource variables through Kubernetes `Secret` and `ConfigMap` values instead of baking them into the image.

## Transaction Learning Point

`MatchService.recordMatch(...)` is annotated with `@Transactional`.

When a result is posted, the service:

1. Validates teams and score.
2. Saves the match result.
3. Recalculates all standings rows.

These operations are committed together. If any runtime exception occurs before the method finishes, the saved match and recalculated standings are rolled back together.

Try posting a match with a negative score from an API client to see validation reject the transaction.
