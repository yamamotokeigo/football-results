# Football Results App 設計メモ / Architecture Notes

## 全体像 / Overview

このアプリは、サッカーの試合結果を登録し、順位表を表示する学習用アプリです。  
This is a small learning app for recording football match results and showing league standings.

```text
ブラウザ / Browser
  |
  | React + Vite
  v
Frontend: frontend/src/main.jsx
  |
  | HTTP JSON API
  v
Backend: Spring Boot /api
  |
  | Controller -> Service -> Repository
  v
H2 インメモリDB / H2 in-memory database
```

## 主な構成 / Main Parts

| 領域 / Area | パス / Path | 役割 / Role |
| --- | --- | --- |
| フロントエンド / Frontend | `frontend/` | 試合結果、順位表、試合登録・削除の画面。 / React UI for results, standings, adding matches, and deleting matches. |
| API | `backend/src/main/java/com/example/football/web/` | `/api` 配下の REST エンドポイント。 / REST endpoints under `/api`. |
| 業務ロジック / Business logic | `backend/src/main/java/com/example/football/service/MatchService.java` | 試合登録・削除と順位表の再計算を担当。 / Handles match create/delete and standings recalculation. |
| ドメイン / Domain | `backend/src/main/java/com/example/football/domain/` | `Team`、`MatchResult`、`Standing` の JPA Entity。 / JPA entities for `Team`, `MatchResult`, and `Standing`. |
| 永続化 / Persistence | `backend/src/main/java/com/example/football/repository/` | Spring Data JPA Repository。 / Spring Data JPA repositories. |
| 初期データ / Seed data | `backend/src/main/java/com/example/football/config/DataInitializer.java` | 起動時にサンプルデータを投入。 / Seeds sample data on startup. |
| CORS | `backend/src/main/java/com/example/football/config/CorsConfig.java` | Vite のローカル画面から API を呼べるようにする設定。 / Allows the local Vite frontend to call the API. |

## データモデル / Data Model

```text
Team
  id
  name
  shortName

MatchResult
  id
  matchDate
  homeTeam -> Team
  awayTeam -> Team
  homeScore
  awayScore
  venue

Standing
  id
  team -> Team
  played
  wins
  draws
  losses
  goalsFor
  goalsAgainst
  goalDifference
  points
```

## API

| Method | Endpoint | 内容 / Purpose |
| --- | --- | --- |
| `GET` | `/api/teams` | チーム一覧を取得。 / Return all teams. |
| `GET` | `/api/matches` | 試合結果を新しい順に取得。 / Return match results, newest first. |
| `POST` | `/api/matches` | 試合結果を登録し、順位表を再計算。 / Save a match result and recalculate standings. |
| `DELETE` | `/api/matches/{id}` | 試合結果を削除し、順位表を再計算。 / Delete a match result and recalculate standings. |
| `GET` | `/api/standings` | 順位付きの順位表を取得。 / Return ranked standings. |

## 処理の流れ / Transaction Flow

試合を登録する場合 / When creating a match:

1. `FootballController.createMatch` がリクエストを受け取る。 / `FootballController.createMatch` receives the request.
2. `MatchService.recordMatch` がホームとアウェイが別チームか、存在するチームかを確認する。 / `MatchService.recordMatch` validates that teams are different and exist.
3. `MatchResult` を保存する。 / Saves the `MatchResult`.
4. 全 `Standing` をリセットし、保存済みの全試合から順位表を作り直す。 / Resets all `Standing` rows and rebuilds them from saved matches.
5. `@Transactional` により、試合保存と順位表更新はまとめてコミットされる。 / `@Transactional` commits the match save and standings update together.

試合を削除する場合も、`MatchService.deleteMatch` で削除と順位表再計算を同じトランザクション内で行います。  
When deleting a match, `MatchService.deleteMatch` also deletes the match and recalculates standings in the same transaction.

## 実行時設定 / Runtime Configuration

| 設定 / Setting | 現在値 / Current value | ファイル / File |
| --- | --- | --- |
| Backend port | `8081` | `backend/src/main/resources/application.yml` |
| Database | H2 in-memory `football` | `backend/src/main/resources/application.yml` |
| H2 console | `/h2-console` | `backend/src/main/resources/application.yml` |
| Frontend dev port | Vite default `5173` | `frontend/package.json` |
| API base URL | `http://localhost:8081/api` | `frontend/src/main.jsx` |

## 今後の改善候補 / Future Improvements

- H2 はインメモリDBなので、Backend を再起動するとデータは初期化されます。 / H2 is in-memory, so data is reset when the backend restarts.
- 現状は試合登録・削除のたびに全試合から順位表を再計算します。 / Standings are currently rebuilt from all matches after every create/delete.
- 自動テストはまだありません。まずは `MatchService` のテスト追加が効果的です。 / There are no automated tests yet. `MatchService` tests would be the best first step.
