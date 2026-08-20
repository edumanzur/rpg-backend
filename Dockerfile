# --- Build stage -----------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy just the Maven wrapper/pom first so dependency resolution is cached
# across builds when only source files change.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline

COPY src src
RUN ./mvnw -q -B package -DskipTests

# --- Runtime stage -----------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Non-root user for the running process.
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /app/target/*.jar app.jar

# Koyeb (and most PaaS platforms) inject PORT at runtime; application-prod.yml
# reads it as server.port: ${PORT:8081}. EXPOSE is documentation only, it
# does not itself bind the port.
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
