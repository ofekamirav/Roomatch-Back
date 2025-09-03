
FROM gradle:7.6-jdk11 AS build

WORKDIR /home/gradle/src

COPY . .

RUN chmod +x ./gradlew

RUN ./gradlew buildFatJar --no-daemon

FROM openjdk:11-jre-slim

EXPOSE 8080

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/roomatch-server.jar

ENTRYPOINT ["java", "-jar", "/app/roomatch-server.jar"]