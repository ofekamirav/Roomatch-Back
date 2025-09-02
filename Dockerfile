FROM gradle:jdk21 AS build

WORKDIR /home/gradle/project

COPY . .


RUN ./gradlew shadowJar


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /home/gradle/project/deploy/*.jar app.jar

CMD ["java", "-jar", "app.jar"]