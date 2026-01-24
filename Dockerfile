FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /build

COPY . .
RUN chmod +x gradlew && ./gradlew clean bootJar -x test

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=builder /build/build/libs/*.jar connext.jar

EXPOSE 8080
CMD ["java", "-jar", "connext.jar"]