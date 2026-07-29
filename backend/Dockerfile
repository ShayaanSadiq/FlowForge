# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY core/pom.xml core/
COPY api/pom.xml api/
COPY worker/pom.xml worker/
RUN mvn -B dependency:go-offline -pl api,worker -am

COPY core core
COPY api api
COPY worker worker
RUN mvn -B package -DskipTests -pl api,worker -am

# API runtime
FROM eclipse-temurin:21-jre-alpine AS api
WORKDIR /app
COPY --from=build /app/api/target/flowforge-api-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# Worker runtime
FROM eclipse-temurin:21-jre-alpine AS worker
WORKDIR /app
COPY --from=build /app/worker/target/flowforge-worker-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
