FROM gradle:8.7-jdk21 AS build
WORKDIR /app

COPY gradle gradle
COPY gradlew ./
COPY settings.gradle.kts ./
COPY build.gradle.kts ./
COPY gradle.properties ./
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon || true

COPY . .

RUN ./gradlew clean shadowJar --no-daemon

RUN set -e; \
    echo "=== DEBUG: deploy ==="; ls -lah deploy || true; \
    cp "$(ls -t deploy/*.jar | head -n1)" /app/app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/app.jar /app/app.jar

EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-Xms64m -Xmx512m -Dio.ktor.development=false"
ENTRYPOINT ["java","-jar","/app/app.jar"]
