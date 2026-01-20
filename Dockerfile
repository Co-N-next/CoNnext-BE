FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY connext.jar .
EXPOSE 8080
CMD ["java", "-jar", "connext.jar"]

