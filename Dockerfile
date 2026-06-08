# =========================
# Build stage
# =========================
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /build

COPY . .

RUN chmod +x mvnw && \
    ./mvnw clean package \
    -DskipTests \
    -Dcheckstyle.skip=true

# =========================
# Runtime stage
# =========================
FROM eclipse-temurin:17-jre

LABEL project="spring-petclinic"
LABEL author="Rhabby"

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=mysql

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
