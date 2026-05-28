FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
RUN apk add --no-cache wget
RUN addgroup -S app && adduser -S app -G app

COPY --from=build /workspace/target/*.jar app.jar

USER app
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
