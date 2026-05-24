# 一から実装した場合のコマンドメモ / Setup Commands From Scratch

空のフォルダから同じ構成を作る場合のコマンド例です。  
These are example commands to recreate the same structure from an empty folder.

## 1. フォルダ作成 / Create Folders

```powershell
mkdir football-results-app
cd football-results-app
mkdir backend
mkdir frontend
```

## 2. Spring Boot Backend 作成 / Create Spring Boot Backend

Java 17 / Spring Boot 3 で、以下の依存関係を使います。  
Use Java 17 / Spring Boot 3 with these dependencies:

- Spring Web
- Spring Data JPA
- Validation
- H2 Database

Spring Initializr をコマンドで使う例 / Example using Spring Initializr:

```powershell
curl "https://start.spring.io/starter.zip?type=maven-project&language=java&bootVersion=3.3.5&baseDir=backend&groupId=com.example&artifactId=football-results-api&name=football-results-api&description=Football%20match%20results%20and%20standings%20API&packageName=com.example.football&packaging=jar&javaVersion=17&dependencies=web,data-jpa,validation,h2" -o backend.zip
tar -xf backend.zip
Remove-Item backend.zip
```

Backend のコードを次の配下に作成します。  
Create backend source files under:

```text
backend/src/main/java/com/example/football/
```

主なファイル / Key files:

```text
FootballResultsApplication.java
config/CorsConfig.java
config/DataInitializer.java
domain/Team.java
domain/MatchResult.java
domain/Standing.java
repository/TeamRepository.java
repository/MatchResultRepository.java
repository/StandingRepository.java
service/MatchService.java
web/FootballController.java
web/ApiExceptionHandler.java
web/dto/*.java
```

設定ファイル / Configuration file:

```text
backend/src/main/resources/application.yml
```

Backend 起動 / Run backend:

```powershell
cd backend
mvn spring-boot:run
```

API 確認 / Verify API:

```powershell
curl http://localhost:8081/api/teams
curl http://localhost:8081/api/matches
curl http://localhost:8081/api/standings
```

## 3. React Frontend 作成 / Create React Frontend

プロジェクトルートから実行します。  
Run from the project root.

```powershell
cd frontend
npm create vite@latest . -- --template react
npm install
npm install lucide-react
```

次のファイルを実装します。  
Implement these files:

```text
frontend/src/main.jsx
frontend/src/styles.css
frontend/vite.config.js
```

Frontend 起動 / Run frontend:

```powershell
npm run dev
```

ブラウザで開くURL / Open in browser:

```text
http://localhost:5173
```

## 4. ビルド確認 / Build Checks

Backend:

```powershell
cd backend
mvn test
```

Frontend:

```powershell
cd frontend
npm run build
```

## 5. Git 初期化と Push / Git Init And Push

プロジェクトルートから実行します。  
Run from the project root.

```powershell
git init
git add .gitignore README.md backend frontend docs
git commit -m "Document football results app architecture"
git branch -M main
git remote add origin <YOUR_GITHUB_REPOSITORY_URL>
git push -u origin main
```

すでに remote がある場合 / If a remote already exists:

```powershell
git remote set-url origin <YOUR_GITHUB_REPOSITORY_URL>
git push -u origin main
```
