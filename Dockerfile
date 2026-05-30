# Stage 1: Build
FROM maven:3.9.0-eclipse-temurin-17-alpine AS build
RUN apk add --no-cache git
WORKDIR /app

# Clone and build
RUN git clone https://github.com/Sajal-Prince/ingress-service.git .
RUN git submodule update --init --recursive
RUN mvn clean install -f project-pay-dto/submodule-dto/pom.xml -DskipTests
RUN mvn clean package -DskipTests

# Stage 2: Minimal Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar
COPY --from=build /app/target/*.jar app.jar

# IMPORTANT: Copy the truststore from the build stage to the final image
COPY --from=build /app/src/main/resources/client.truststore.jks /app/certs/client.truststore.jks

ENTRYPOINT ["java", "-jar", "app.jar"]