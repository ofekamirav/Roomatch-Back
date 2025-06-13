FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY app.jar .
COPY .env .

CMD ["java", "-jar", "app.jar"]
