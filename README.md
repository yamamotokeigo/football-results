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

## Transaction Learning Point

`MatchService.recordMatch(...)` is annotated with `@Transactional`.

When a result is posted, the service:

1. Validates teams and score.
2. Saves the match result.
3. Recalculates all standings rows.

These operations are committed together. If any runtime exception occurs before the method finishes, the saved match and recalculated standings are rolled back together.

Try posting a match with a negative score from an API client to see validation reject the transaction.
